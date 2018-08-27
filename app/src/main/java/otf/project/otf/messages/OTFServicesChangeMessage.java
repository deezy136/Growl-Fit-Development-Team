package otf.project.otf.messages;

import java.util.List;

import otf.project.otf.models.base.OTFService;

/**
 * Created by denismalcev on 03.06.17.
 */

public class OTFServicesChangeMessage {

    private final List<OTFService> services;

    public OTFServicesChangeMessage(List<OTFService> services) {
        this.services = services;
    }

    public List<OTFService> getServices() {
        return services;
    }
}
