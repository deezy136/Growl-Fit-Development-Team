package otf.project.otf.protocol;

import otf.project.otf.utils.GsonManager;

/**
 * Created by denismalcev on 07.06.17.
 */

public class SocketInvitationCommand extends SocketCommand {

    private final int group;

    public SocketInvitationCommand(int group) {
        super(SocketCommandProtocol.COMMAND_INVITE);
        this.group = group;
    }

    public int getGroup() {
        return group;
    }

    @Override
    public String toString() {
        return GsonManager.getInstance().getGson().toJson(this);
    }
}
