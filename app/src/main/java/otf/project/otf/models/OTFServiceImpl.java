package otf.project.otf.models;

import otf.project.otf.models.base.OTFService;

/**
 * Created by denismalcev on 08.06.17.
 */

public class OTFServiceImpl extends OTFService {

    private final String name;
    private final String ip;

    private int socketPort;
    private long createTime;

    public OTFServiceImpl(String id, String name, String ip, int port, long createTime) {
        super(id);
        this.name = name;
        this.ip = ip;
        socketPort = port;
        this.createTime =createTime;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getIp() {
        return ip;
    }

    @Override
    public int getPort() {
        return socketPort;
    }

    @Override
    public long getCreateTime() {
        return createTime;
    }
}