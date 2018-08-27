package otf.project.otf.protocol;

import otf.project.otf.utils.GsonManager;

/**
 * Created by denismalcev on 07.06.17.
 */

public class SocketStopStreamingCommand extends SocketCommand {


    public SocketStopStreamingCommand() {
        super(SocketCommandProtocol.COMMAND_STOP_STREAMING);
    }

    @Override
    public String toString() {
        return GsonManager.getInstance().getGson().toJson(this);
    }
}
