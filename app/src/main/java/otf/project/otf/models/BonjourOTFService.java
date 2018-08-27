package otf.project.otf.models;

import de.mannodermaus.rxbonjour.BonjourService;
import otf.project.otf.models.base.OTFService;
import otf.project.otf.utils.BonjourOTFServiceUtils;
import otf.project.otf.utils.Constants;

/**
 * Created by denismalcev on 03.06.17.
 */

public class BonjourOTFService extends OTFService {

    private final String name;
    private final String ip;
    private final String groupId;
    private final long createTime;

    private int socketPort;


    public BonjourOTFService(BonjourService bonjourService) {
        super(BonjourOTFServiceUtils.getTxtRecord(bonjourService, Constants.SERVICE_UUID_KEY));
        this.name = bonjourService.getName();
        if (bonjourService.getV6Host() != null) {
            this.ip = bonjourService.getV6Host().getHostAddress();
        } else {
            this.ip = bonjourService.getV4Host() != null ? bonjourService.getV4Host().getHostAddress() : null;
        }

        socketPort = Integer.valueOf(BonjourOTFServiceUtils.getTxtRecord(bonjourService, Constants.SOCKET_RECEIVER_PORT, "-1"));
        groupId = BonjourOTFServiceUtils.getTxtRecord(bonjourService, Constants.CLIENT_GROUP_ID, null);
        createTime = Long.valueOf(BonjourOTFServiceUtils.getTxtRecord(bonjourService, Constants.SERVICE_START_TIME, "0"));
    }

    @Override
    public long getCreateTime() {
        return createTime;
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
    public String getGroupId() {
        return groupId;
    }
}
