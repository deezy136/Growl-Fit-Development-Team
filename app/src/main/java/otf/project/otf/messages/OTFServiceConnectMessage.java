package otf.project.otf.messages;

import otf.project.otf.models.base.OTFService;

/**
 * Created by denismalcev on 03.06.17.
 */

public class OTFServiceConnectMessage {

    private final OTFService service;

    public OTFServiceConnectMessage(OTFService service) {
        this.service = service;
    }

    public OTFService getService() {
        return service;
    }
}
