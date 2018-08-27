package otf.project.otf.protocol;

import otf.project.otf.models.OTFUser;
import otf.project.otf.utils.UserUtils;

/**
 * Created by denismalcev on 07.06.17.
 */

public class SocketCommand {

    private final String type;
    private final OTFUser sender;

    private String ip;
    private int port;

    private String json;

    public SocketCommand(String type) {
        this.type = type;
        this.sender = UserUtils.getUser();
    }

    public String getType() {
        return type;
    }

    public OTFUser getSender() {
        return sender;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
