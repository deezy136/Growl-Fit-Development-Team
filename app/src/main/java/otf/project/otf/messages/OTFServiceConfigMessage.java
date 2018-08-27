package otf.project.otf.messages;

/**
 * Created by denismalcev on 18.09.17.
 */

public class OTFServiceConfigMessage {

    private final int socketPort;
    private final String ipAddress;

    public OTFServiceConfigMessage(int socketPort, String ipAddress) {
        this.socketPort = socketPort;
        this.ipAddress = ipAddress;
    }

    public int getSocketPort() {
        return socketPort;
    }

    public String getIpAddress() {
        return ipAddress;
    }
}
