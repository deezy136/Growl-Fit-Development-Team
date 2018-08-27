package otf.project.otf.networking.udp;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.koushikdutta.async.AsyncDatagramSocket;
import com.koushikdutta.async.AsyncServer;
import com.koushikdutta.async.ByteBufferList;
import com.koushikdutta.async.DataEmitter;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.callback.DataCallback;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import otf.project.otf.models.base.OTFService;
import otf.project.otf.protocol.SocketCommand;
import otf.project.otf.utils.Constants;

/**
 * Created by reweber on 12/20/14.
 */
public class Client {

    private final InetSocketAddress host;
    private AsyncDatagramSocket asyncDatagramSocket;

    public Client(OTFService service) {
        try {
            this.host = new InetSocketAddress(Inet4Address.getByName(service.getIp()), service.getPort());
            setup();
        } catch (UnknownHostException exc) {
            throw new RuntimeException(exc);
        }
    }

    private void setup() {
        try {
            asyncDatagramSocket = AsyncServer.getDefault().connectDatagram(host);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        asyncDatagramSocket.setDataCallback(new DataCallback() {
            @Override
            public void onDataAvailable(DataEmitter emitter, ByteBufferList bb) {
                System.out.println("[Client] Received Message " + new String(bb.getAllByteArray()));
            }
        });

        asyncDatagramSocket.setClosedCallback(new CompletedCallback() {
            @Override
            public void onCompleted(Exception ex) {
                if(ex != null) ex.printStackTrace();
                System.out.println("[Client] Successfully closed connection");
            }
        });

        asyncDatagramSocket.setEndCallback(new CompletedCallback() {
            @Override
            public void onCompleted(Exception ex) {
                if(ex != null) ex.printStackTrace();
                System.out.println("[Client] Successfully end connection");
            }
        });
    }

    public void send(SocketCommand command) {

        CustomEvent customEvent = new CustomEvent("SendCommandInClient");

        customEvent.putCustomAttribute("commandId", command.getIp());
        customEvent.putCustomAttribute("hostIp", host.getAddress().getHostAddress());
        customEvent.putCustomAttribute("command", command.getClass().getName());
        Answers.getInstance().logCustom(customEvent);

        byte[] commandBytes = command.toString().getBytes();
        byte[] packageBytes = new byte[commandBytes.length + 1];
        System.arraycopy(commandBytes, 0, packageBytes, 1, commandBytes.length);
        packageBytes[0] = Constants.AudioConfig.DATA_PACKAGE;
        asyncDatagramSocket.send(host, ByteBuffer.wrap(packageBytes));
    }
}
