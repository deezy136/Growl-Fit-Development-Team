package otf.project.otf.networking.udp;

import com.koushikdutta.async.AsyncDatagramSocket;
import com.koushikdutta.async.AsyncServer;
import com.koushikdutta.async.ByteBufferList;
import com.koushikdutta.async.DataEmitter;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.callback.DataCallback;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

/**
 * Created by reweber on 12/20/14.
 */
public class Server {

    public interface OnDataCallback {
        void onDataReceived(byte[] data);
    }

    private InetSocketAddress host;
    private AsyncDatagramSocket asyncDatagramSocket;
    private final OnDataCallback callback;

    public Server(String host, int port, OnDataCallback callback) {
        this.host = new InetSocketAddress(host, port);
        this.callback = callback;
        setup();
    }

    private void setup() {

        try {
            asyncDatagramSocket = AsyncServer.getDefault().openDatagram(host, true);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        asyncDatagramSocket.setDataCallback(new DataCallback() {
            @Override
            public void onDataAvailable(DataEmitter emitter, ByteBufferList bb) {
                if (callback != null) {
                    callback.onDataReceived(bb.getAllByteArray());
                }
            }
        });

        asyncDatagramSocket.setClosedCallback(new CompletedCallback() {
            @Override
            public void onCompleted(Exception ex) {
                if (ex != null) throw new RuntimeException(ex);
            }
        });

        asyncDatagramSocket.setEndCallback(new CompletedCallback() {
            @Override
            public void onCompleted(Exception ex) {
                if (ex != null) throw new RuntimeException(ex);
            }
        });
    }

    public void disconnect() {
        if (asyncDatagramSocket != null) {
            try {
                asyncDatagramSocket.disconnect();
            } catch (IOException exc) {

            }
        }
    }

    public void send(String msg) {
        asyncDatagramSocket.send(host, ByteBuffer.wrap(msg.getBytes()));
    }

    public int getPort() {
        return host.getPort();
    }
}
