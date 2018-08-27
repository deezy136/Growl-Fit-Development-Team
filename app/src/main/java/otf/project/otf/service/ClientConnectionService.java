package otf.project.otf.service;

import android.app.Notification;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.mannodermaus.rxbonjour.BonjourBroadcastConfig;
import de.mannodermaus.rxbonjour.BonjourEvent;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import otf.project.otf.R;
import otf.project.otf.messages.OTFServiceConfigMessage;
import otf.project.otf.messages.OTFServicesChangeMessage;
import otf.project.otf.models.BonjourOTFService;
import otf.project.otf.models.OTFServiceState;
import otf.project.otf.models.OTFUser;
import otf.project.otf.models.base.OTFService;
import otf.project.otf.utils.AudioReceiver;
import otf.project.otf.utils.ConnectionUtils;
import otf.project.otf.utils.Constants;
import otf.project.otf.utils.UserUtils;

/**
 * Created by denismalcev on 22.05.17.
 */

public class ClientConnectionService extends BaseConnectionService {

    public static final String RESTART_CLIENT_BROADCAST= "connection.restart_client_broadcast";

    private List<OTFService> hostServices = new ArrayList<>();

    private static final int FOREGROUND_ID = 1331;
    private boolean isInitialConfigured = false;

    private Observable discoverySubscription;
    private Completable broadcastSubscription;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && STOP.equals(intent.getAction())) {
            if (audioReceiver != null) {
                audioReceiver.stopSpeakers();
            }
            stopForeground(true);
            stopSelf();
            return super.onStartCommand(intent, flags, startId);
        } else if (intent != null && RESTART_CLIENT_BROADCAST.equals(intent.getAction())) {
            restartConnections();
        }

        initialConfiguration();
        startForeground(FOREGROUND_ID, buildForegroundNotification(getString(R.string.audio_session_inactive)));
        return super.onStartCommand(intent, flags, startId);
    }

    private Notification buildForegroundNotification(String statusMessage) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(statusMessage);
        return builder.build();
    }

    private void initialConfiguration() {
        if (isInitialConfigured)
            return;

        isInitialConfigured = true;
        int freePort = startLocalServer();
        startServiceDiscovery();
        startInvitationService(freePort);
    }

    private void startServiceDiscovery() {
        hostServices.clear();
        if (ConnectionUtils.isWifiConnectionEnabled()) {
            discoverySubscription = rxBonjour.newDiscovery("_http._tcp");
            discoverySubscription.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<BonjourEvent>() {
                        @Override
                        public void accept(BonjourEvent bonjourEvent) throws Exception {

                            OTFServiceState state = (bonjourEvent instanceof BonjourEvent.Added) ? OTFServiceState.ADDED : OTFServiceState.REMOVED;
                            OTFService service = new BonjourOTFService(bonjourEvent.getService());
                            onServiceChanged(service, state);
                        }
                    });
        }
    }

    private void startInvitationService(int receiverPort) {
        if (ConnectionUtils.isWifiConnectionEnabled()) {

            OTFUser user = UserUtils.getUser();
            int localPort = ConnectionUtils.findFreePort();

            Map<String,String> txtRecords = new HashMap<>();
            txtRecords.put(Constants.SERVICE_START_TIME, String.valueOf(System.currentTimeMillis()));
            txtRecords.put(Constants.SERVICE_UUID_KEY, user.getId());

            if (receiverPort > 0) {
                txtRecords.put(Constants.SOCKET_RECEIVER_PORT, String.valueOf(receiverPort));
            }
            if (user.getGroup() > 0) {
                txtRecords.put(Constants.CLIENT_GROUP_ID, String.valueOf(user.getGroup()));
            }
            BonjourBroadcastConfig config = new BonjourBroadcastConfig("_otfinvitation._tcp",
                    user.getName(),
                    null,
                    localPort,
                    txtRecords);

            broadcastSubscription = rxBonjour.newBroadcast(config);
            broadcastSubscription.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe();
            EventBus.getDefault().post(new OTFServiceConfigMessage(receiverPort, ConnectionUtils.getIPAddress()));
        }
    }

    private void onServiceChanged(OTFService service, OTFServiceState state) {
        if (state == OTFServiceState.ADDED) {
            if (!hostServices.contains(service)) {
                hostServices.add(service);
            }
        } else {
            hostServices.remove(service);
        }
        EventBus.getDefault().post(new OTFServicesChangeMessage(hostServices));
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    protected void restartServicesUpdates() {
        if (discoverySubscription != null) {
            discoverySubscription.unsubscribeOn(Schedulers.io());
        }
        startServiceDiscovery();
    }

    @Override
    protected void stopConnections() {
        super.stopConnections();
        if (discoverySubscription != null) {
            discoverySubscription.unsubscribeOn(Schedulers.io());
            discoverySubscription = null;
        }

        if (broadcastSubscription != null) {
            broadcastSubscription.unsubscribeOn(Schedulers.io());
            broadcastSubscription = null;
        }
    }

    @Override
    protected void restartConnections() {
        int receiverPort = getReceiverPort();
        if (receiverPort == -1) {
            return;
        }

        restartServicesUpdates();

        if (broadcastSubscription != null) {
            broadcastSubscription.unsubscribeOn(Schedulers.io());
        }
        startInvitationService(receiverPort);
    }

    @Override
    protected boolean isHost() {
        return false;
    }

    private AudioReceiver audioReceiver;

    @Override
    public void onDataReceived(byte[] data) {
        if (data[0] == Constants.AudioConfig.AUDIO_PACKAGE) {
            if (audioReceiver != null) {
                audioReceiver.onDataReceived(data);
            }
        } else {
            super.onDataReceived(data);
        }
    }

    @Override
    protected void onStartStreaming() {
        startForeground(FOREGROUND_ID, buildForegroundNotification(getString(R.string.audio_session_active)));
        audioReceiver = new AudioReceiver();
        audioReceiver.startSpeakers();
    }

    @Override
    protected void onStopStreaming() {
        audioReceiver.stopSpeakers();
        audioReceiver = null;
        startForeground(FOREGROUND_ID, buildForegroundNotification(getString(R.string.audio_session_inactive)));
    }
}
