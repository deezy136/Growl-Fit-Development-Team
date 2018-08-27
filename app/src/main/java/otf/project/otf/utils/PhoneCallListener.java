package otf.project.otf.utils;

import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * Created by denis on 15.02.2018.
 */

public class PhoneCallListener extends PhoneStateListener {

    private boolean isPhoneCalling = false;

    @Override
    public void onCallStateChanged(int state, String incomingNumber) {

        if (TelephonyManager.CALL_STATE_RINGING == state) {

        }

        if (TelephonyManager.CALL_STATE_OFFHOOK == state) {
            isPhoneCalling = true;
        }

        if (TelephonyManager.CALL_STATE_IDLE == state) {
            if (isPhoneCalling) {
                isPhoneCalling = false;
            }

        }
    }
}
