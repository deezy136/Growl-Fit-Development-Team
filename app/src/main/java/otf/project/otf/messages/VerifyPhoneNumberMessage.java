package otf.project.otf.messages;

/**
 * Created by denismalcev on 04.06.17.
 */

public class VerifyPhoneNumberMessage {

    private final String phoneNumber;
    private final String name;

    public VerifyPhoneNumberMessage(String phoneNumber, String name) {
        this.phoneNumber = phoneNumber;
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getName() {
        return name;
    }
}
