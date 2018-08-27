package otf.project.otf.service;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.session.MediaSessionManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import de.mannodermaus.rxbonjour.BonjourBroadcastConfig;
import de.mannodermaus.rxbonjour.BonjourEvent;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import otf.project.otf.R;
import otf.project.otf.messages.OTFServicesChangeMessage;
import otf.project.otf.models.BonjourOTFService;
import otf.project.otf.models.OTFClient;
import otf.project.otf.models.OTFServiceImpl;
import otf.project.otf.models.OTFServiceState;
import otf.project.otf.models.OTFUser;
import otf.project.otf.models.base.OTFService;
import otf.project.otf.protocol.SocketStartStreamingCommand;
import otf.project.otf.protocol.SocketStopStreamingCommand;
import otf.project.otf.receiver.MediaButtonReceiver;
import otf.project.otf.utils.AudioCall;
import otf.project.otf.utils.ClientUtils;
import otf.project.otf.utils.ConnectionUtils;
import otf.project.otf.utils.Constants;
import otf.project.otf.utils.HandsetButtonPressHandler;
import otf.project.otf.utils.PhoneCallListener;
import otf.project.otf.utils.UserUtils;

/**
 * Created by denismalcev on 22.05.17.
 */

public class ConnectionService extends BaseConnectionService implements
        HandsetButtonPressHandler.HandsetButtonListener {

    private AudioCall audioCall;

    public static final String RESTART_DISCOVERY = "connection.restart_discovery";
    public static final String START_RECORD = "start_record";

    private static final int FOREGROUND_ID = 1332;
    private boolean isInitialConfigured = false;

    private List<OTFService> pendingConnectionServices = new ArrayList<>();

    private Observable invitationDiscovery;
    private Completable broadcastSubscription;

    private MediaSessionManager mediaSessionManager;
    private MediaSessionCompat mediaSession;
    private HandsetButtonPressHandler pressHandler;

    private AudioManager audioManager;
    private ComponentName componentName;

    @Override
    public void onCreate() {
        super.onCreate();

        componentName = new ComponentName(getPackageName(), MediaButtonReceiver.class.getName());
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (STOP.equals(intent.getAction())) {
            stopForeground(true);
            stopSelf();
            return super.onStartCommand(intent, flags, startId);
        } else if (RESTART_DISCOVERY.equals(intent.getAction())) {
            restartServicesUpdates();
        }
        initialConfiguration();
        updateForegroud(buildForegroundNotification(getString(R.string.audio_session_inactive)));
        return super.onStartCommand(intent, flags, startId);
    }

    private void updateForegroud(Notification notification) {
        startForeground(FOREGROUND_ID, notification);
    }

    private Notification buildForegroundNotification(String statusMessage) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("OTF Client")
                .setContentText(statusMessage);
        return builder.build();
    }

    private void initialConfiguration() {
        if (isInitialConfigured)
            return;

        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(new CallListener(), PhoneStateListener.LISTEN_CALL_STATE);

        isInitialConfigured = true;

        pressHandler = new HandsetButtonPressHandler(this);

        mediaSessionManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);

        mediaSession = new MediaSessionCompat(this, "OTF_Session");
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS);
        mediaSession.setCallback(earphoneButtonCallback);

        mediaSession.setActive(true);
        mediaSession.setPlaybackState(new PlaybackStateCompat.Builder().setState(PlaybackStateCompat.STATE_CONNECTING, -1, 1.0f).build());

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                updateMediaSession();
            }
        }, 0, 2000);

        startServer();
        startPendingInvitationDiscovery();
    }

    private void updateMediaSession() {
        mediaSession.setActive(false);
        mediaSession.setPlaybackState(new PlaybackStateCompat.Builder().setState(PlaybackStateCompat.STATE_NONE, -1, 1.0f).build());

        mediaSession.setActive(true);
        mediaSession.setPlaybackState(new PlaybackStateCompat.Builder().setState(PlaybackStateCompat.STATE_CONNECTING, -1, 1.0f).build());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startServer() {
        if (ConnectionUtils.isWifiConnectionEnabled()) {
            OTFUser user = UserUtils.getUser();
            int serverPort = startLocalServer();

            Map<String,String> txtRecords = new HashMap<>();
            txtRecords.put(Constants.SERVICE_UUID_KEY, user.getId());
            if (serverPort > 0) {
                txtRecords.put(Constants.SOCKET_RECEIVER_PORT, String.valueOf(serverPort));
            }
            BonjourBroadcastConfig config = new BonjourBroadcastConfig("_http._tcp",
                    user.getName(),
                    null,
                    serverPort,
                    txtRecords);

            broadcastSubscription = rxBonjour.newBroadcast(config);
            broadcastSubscription.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe();
        }
    }

    private void startPendingInvitationDiscovery() {
        pendingConnectionServices.clear();
        if (ConnectionUtils.isWifiConnectionEnabled()) {
            invitationDiscovery = rxBonjour.newDiscovery("_otfinvitation._tcp");
            invitationDiscovery.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<BonjourEvent>() {
                        @Override
                        public void accept(BonjourEvent bonjourEvent) throws Exception {

                            OTFServiceState state = (bonjourEvent instanceof BonjourEvent.Added) ? OTFServiceState.ADDED : OTFServiceState.REMOVED;
                            OTFService service = new BonjourOTFService(bonjourEvent.getService());
                            onPendingInvitationClientsChanged(service, state);
                        }
                    });
        }
    }

    private void onPendingInvitationClientsChanged(OTFService service, OTFServiceState state) {
        if (state == OTFServiceState.ADDED) {
            if (!pendingConnectionServices.contains(service)) {
                pendingConnectionServices.add(service);
            } else {
                int index = pendingConnectionServices.indexOf(service);
                OTFService existingService = pendingConnectionServices.get(index);
                if (existingService.getCreateTime() < service.getCreateTime()) {
                    pendingConnectionServices.set(index, service);
                }
            }
        } else {
            pendingConnectionServices.remove(service);
        }
        EventBus.getDefault().post(new OTFServicesChangeMessage(pendingConnectionServices));
    }

    @Override
    protected boolean isHost() {
        return true;
    }

    @Override
    protected void restartServicesUpdates() {
        if (invitationDiscovery != null) {
            invitationDiscovery.unsubscribeOn(Schedulers.io());
        }
        startPendingInvitationDiscovery();
    }

    @Override
    protected void stopConnections() {
        super.stopConnections();
        if (invitationDiscovery != null) {
            invitationDiscovery.unsubscribeOn(Schedulers.io());
            invitationDiscovery = null;
        }

        if (broadcastSubscription != null) {
            broadcastSubscription.unsubscribeOn(Schedulers.io());
            broadcastSubscription = null;
        }
    }

    @Override
    protected void restartConnections() {
        restartServicesUpdates();

        if (broadcastSubscription != null) {
            broadcastSubscription.unsubscribeOn(Schedulers.io());
        }
        startServer();
    }

    public void startAudioSessionForGroup(int group) {
        if (group > 2)
            return;

        if (audioCall == null) {
            List<OTFClient> clients = ClientUtils.getClients(group);
            if (clients.size() > 0) {
                List<InetSocketAddress> socketAddresses = new ArrayList<>();
                List<OTFService> receivers = new ArrayList<>();
                for (OTFClient client : clients) {
                    for (OTFService service : pendingConnectionServices) {
                        if (service.getId() != null && client.getId() != null && service.getId().equals(client.getId())) {
                            if (service.getPort() > 0) {
                                socketAddresses.add(new InetSocketAddress(service.getIp(), service.getPort()));
                                receivers.add(service);
                            }
                        }
                    }
                }
                if (socketAddresses.size() > 0) {

                    for (OTFService service : receivers) {
                        sendCommand(new SocketStartStreamingCommand(), service);
                    }

                    audioCall = new AudioCall(true, socketAddresses);
                    audioCall.startCall();

                    updateForegroud(buildForegroundNotification(getString(R.string.audio_session_active_group, String.valueOf(group))));
                } else {
                    Toast.makeText(this, "Error starting audio session for group " + group + ". No online clients!", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "Error starting audio session for group " + group + ". No clients in group!", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onClick(int count) {
        if (audioCall != null) {
           stopAudioSession();
        } else {
            startAudioSessionForGroup(count);
        }
    }

    private void stopAudioSession() {
        if (audioCall != null) {
            updateForegroud(buildForegroundNotification(getString(R.string.audio_session_inactive)));
            audioCall.endCall();

            for (InetSocketAddress address : audioCall.getClientList()) {
                sendCommand(new SocketStopStreamingCommand(),
                        new OTFServiceImpl("", "", address.getAddress().getHostAddress(),
                                address.getPort(), System.currentTimeMillis()));
            }
            audioCall = null;
        }
    }

    private MediaSessionCompat.Callback earphoneButtonCallback = new MediaSessionCompat.Callback() {
        @Override
        public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
            KeyEvent event = mediaButtonEvent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            pressHandler.handleClick(event);
            return true;
        }
    };

    private class CallListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {

            if (TelephonyManager.CALL_STATE_RINGING == state) {

            }

            if (TelephonyManager.CALL_STATE_OFFHOOK == state || TelephonyManager.CALL_STATE_RINGING == state) {
                stopAudioSession();
            }

            if (TelephonyManager.CALL_STATE_IDLE == state) {

            }
        }
    }

}
