package otf.project.otf.protocol;

/**
 * Created by denismalcev on 04.08.17.
 */

public class SocketPingPongCommand extends SocketCommand {

    public SocketPingPongCommand(boolean isPing) {
        super(isPing ? SocketCommandProtocol.COMMAND_PING : SocketCommandProtocol.COMMAND_PONG);
    }
}
