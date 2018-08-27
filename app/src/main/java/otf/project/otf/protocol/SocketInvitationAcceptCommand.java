package otf.project.otf.protocol;

import otf.project.otf.utils.GsonManager;

/**
 * Created by denismalcev on 07.06.17.
 */

public class SocketInvitationAcceptCommand extends SocketCommand {

    private final int group;


    public SocketInvitationAcceptCommand(int group) {
        super(SocketCommandProtocol.COMMAND_INVITATION_ACCEPT);
        this.group = group;
    }

    @Override
    public String toString() {
        return GsonManager.getInstance().getGson().toJson(this);
    }

    public int getGroup() {
        return group;
    }
}
