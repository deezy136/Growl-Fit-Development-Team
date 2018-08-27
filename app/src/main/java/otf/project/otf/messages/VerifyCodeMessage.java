package otf.project.otf.messages;

/**
 * Created by denismalcev on 04.06.17.
 */

public class VerifyCodeMessage {

    private final String code;

    public VerifyCodeMessage(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
