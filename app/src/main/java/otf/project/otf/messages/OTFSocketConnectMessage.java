package otf.project.otf.messages;

/**
 * Created by denismalcev on 06.06.17.
 */

public class OTFSocketConnectMessage {

    private final String host;
    private final int port;

    public OTFSocketConnectMessage(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
}
