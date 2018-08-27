package otf.project.otf.messages;

import otf.project.otf.protocol.SocketCommand;

/**
 * Created by denismalcev on 06.06.17.
 */

public class OTFSocketIncomingCommandMessage {

    private final SocketCommand command;

    public OTFSocketIncomingCommandMessage(SocketCommand command) {
        this.command = command;
    }

    public SocketCommand getCommand() {
        return command;
    }
}
