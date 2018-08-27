package otf.project.otf.protocol;

import otf.project.otf.utils.GsonManager;

/**
 * Created by denismalcev on 07.06.17.
 */

public class SocketStartStreamingCommand extends SocketCommand {


    public SocketStartStreamingCommand() {
        super(SocketCommandProtocol.COMMAND_START_STREAMING);
    }

    @Override
    public String toString() {
        return GsonManager.getInstance().getGson().toJson(this);
    }
}
