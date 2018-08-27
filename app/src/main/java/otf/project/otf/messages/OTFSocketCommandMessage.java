package otf.project.otf.messages;

import otf.project.otf.models.base.OTFService;
import otf.project.otf.protocol.SocketCommand;

/**
 * Created by denismalcev on 06.06.17.
 */

public class OTFSocketCommandMessage {

    private final OTFService service;
    private final SocketCommand command;

    public OTFSocketCommandMessage(OTFService service, SocketCommand command) {
        this.service = service;
        this.command = command;
    }

    public SocketCommand getCommand() {
        return command;
    }

    public OTFService getService() {
        return service;
    }
}
