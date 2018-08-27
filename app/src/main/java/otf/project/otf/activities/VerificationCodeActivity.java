package otf.project.otf.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.nexmo.sdk.NexmoClient;
import com.nexmo.sdk.core.client.ClientBuilderException;
import com.nexmo.sdk.verify.client.VerifyClient;

import io.github.kobakei.grenade.annotation.Navigator;
import otf.project.otf.R;
import otf.project.otf.activities.base.BaseActivity;
import otf.project.otf.utils.Constants;

/**
 * Created by denismalcev on 04.06.17.
 */

@Navigator
public class VerificationCodeActivity extends BaseActivity  {

    private NexmoClient nexmoClient;
    private VerifyClient verifyClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code_verification);
        initNexmo();
    }

    private void initNexmo() {
        try {
            nexmoClient = new NexmoClient.NexmoClientBuilder()
                    .context(this)
                    .applicationId(Constants.NEXMO_APP_ID)
                    .sharedSecretKey(Constants.NEXMO_APP_SECRET)
                    .build();

            verifyClient = new VerifyClient(nexmoClient);
//            verifyClient.addVerifyListener(this);
        } catch (ClientBuilderException exc) {

        }
    }
}
