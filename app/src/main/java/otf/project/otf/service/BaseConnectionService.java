package otf.project.otf.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Arrays;

import de.mannodermaus.rxbonjour.RxBonjour;
import de.mannodermaus.rxbonjour.drivers.jmdns.JmDNSDriver;
import de.mannodermaus.rxbonjour.platforms.android.AndroidPlatform;
import otf.project.otf.messages.OTFServicesSyncMessage;
import otf.project.otf.messages.OTFSocketCommandMessage;
import otf.project.otf.messages.OTFSocketIncomingCommandMessage;
import otf.project.otf.models.OTFClient;
import otf.project.otf.models.OTFUser;
import otf.project.otf.models.base.OTFService;
import otf.project.otf.networking.udp.Client;
import otf.project.otf.networking.udp.Server;
import otf.project.otf.protocol.SocketCommand;
import otf.project.otf.protocol.SocketCommandProtocol;
import otf.project.otf.protocol.SocketInvitationAcceptCommand;
import otf.project.otf.protocol.SocketInvitationCommand;
import otf.project.otf.utils.ClientUtils;
import otf.project.otf.utils.ConnectionUtils;
import otf.project.otf.utils.GsonManager;
import otf.project.otf.utils.IntentFilterCreator;

/**
 * Created by denismalcev on 04.06.17.
 */

public abstract class BaseConnectionService extends Service implements Server.OnDataCallback {

    public static final String STOP = "stop";
    private Server datagramServer;

    private Thread socketServerThread;
    protected RxBonjour rxBonjour;

    @Override
    public void onCreate() {
        super.onCreate();
        rxBonjour = new RxBonjour.Builder()
                .platform(AndroidPlatform.create(this))
                .driver(JmDNSDriver.create())
                .create();
        registerReceiver(connectionReceiver, IntentFilterCreator.create(ConnectivityManager.CONNECTIVITY_ACTION));
        EventBus.getDefault().register(this);
    }
    @Override
    public void onDestroy() {
        stopConnections();
        unregisterReceiver(connectionReceiver);
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    protected abstract boolean isHost();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    protected int startLocalServer() {
        if (datagramServer != null) {
            datagramServer.disconnect();
        }

        int freePort = ConnectionUtils.findFreePort();

        socketServerThread = new Thread(new SocketServerThread(freePort));
        socketServerThread.start();

        return freePort;
    }

    @Subscribe
    public void onSyncOTFSerivices(OTFServicesSyncMessage message) {
        restartServicesUpdates();
    }

    protected abstract void restartServicesUpdates();

    @Subscribe
    public void onSendCommand(OTFSocketCommandMessage message) {
        sendCommand(message.getCommand(), message.getService());
    }

    protected void sendCommand(SocketCommand command, OTFService service) {
        CustomEvent customEvent = new CustomEvent("SendCommand");
        command.setIp(ConnectionUtils.getIPAddress());
        command.setPort(getReceiverPort());

        customEvent.putCustomAttribute("localIp", command.getIp());
        customEvent.putCustomAttribute("command", command.getClass().getName());
        Answers.getInstance().logCustom(customEvent);

        new Thread(new SocketMessageThread(service, command)).start();
    }

    private class SocketServerThread implements Runnable {

        private final int serverPort;

        public SocketServerThread(int serverPort) {
            this.serverPort = serverPort;
        }

        @Override
        public void run() {
            String localIpAddress = ConnectionUtils.getIPAddress();
            datagramServer = new Server(localIpAddress, serverPort, BaseConnectionService.this);
        }
    }

    private class SocketMessageThread implements Runnable {

        private OTFService service;
        private SocketCommand command;

        public SocketMessageThread(OTFService service, SocketCommand command) {
            this.service = service;
            this.command = command;
        }

        @Override
        public void run() {
            new Client(service).send(command);
        }
    }

    private BroadcastReceiver connectionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            if (activeNetwork == null) {
                stopConnections();
            } else {
                boolean isConnected = activeNetwork.isConnectedOrConnecting();
                if (isConnected) {
                    restartConnections();
                }
            }
        }
    };

    protected void stopConnections() {
        if (datagramServer != null) {
            datagramServer.disconnect();
        }
    }

    protected void restartConnections() {

    }

    protected int getReceiverPort() {
        if (datagramServer == null) {
            return -1;
        }
        return datagramServer.getPort();
    }

    @Override
    public void onDataReceived(byte[] data) {
        String message = new String(Arrays.copyOfRange(data, 1, data.length));
        try {
            SocketCommand command = GsonManager.getInstance().getGson().fromJson(message, SocketCommand.class);
            if (command != null) {
                command.setJson(message);

                if (SocketCommandProtocol.COMMAND_INVITE.equals(command.getType())) {
                    SocketInvitationCommand invitationCommand = GsonManager.getInstance().getGson().fromJson(command.getJson(), SocketInvitationCommand.class);
                    if (invitationCommand != null) {
                        EventBus.getDefault().post(new OTFSocketIncomingCommandMessage(invitationCommand));
                    }
                } else if (SocketCommandProtocol.COMMAND_INVITATION_ACCEPT.equals(command.getType())) {
                    SocketInvitationAcceptCommand acceptCommand = GsonManager.getInstance().getGson().fromJson(command.getJson(), SocketInvitationAcceptCommand.class);

                    if (acceptCommand != null) {
                        OTFUser sender = acceptCommand.getSender();
                        String ip = acceptCommand.getIp();
                        int port = acceptCommand.getPort();
                        OTFClient client = new OTFClient(sender.getId(), sender.getName(), acceptCommand.getGroup());

                        ClientUtils.saveClient(client);

                        EventBus.getDefault().post(new OTFSocketIncomingCommandMessage(acceptCommand));
                    }
                } else if (SocketCommandProtocol.COMMAND_START_STREAMING.equals(command.getType())) {
                    onStartStreaming();
                } else if (SocketCommandProtocol.COMMAND_STOP_STREAMING.equals(command.getType())) {
                    onStopStreaming();
                }
            }
        } catch (IllegalStateException exc) {
            //seems that we're trying to decode audio-string
            //ignore
        }
    }

    protected void onStartStreaming() {

    }

    protected void onStopStreaming() {

    }


}
