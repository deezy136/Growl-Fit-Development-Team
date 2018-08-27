package otf.project.otf.messages;

/**
 * Created by denismalcev on 06.06.17.
 */

public class OTFSocketMessage {

    private final String message;

    public OTFSocketMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
