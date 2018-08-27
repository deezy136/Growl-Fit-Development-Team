package otf.project.otf.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;

import com.nexmo.sdk.NexmoClient;
import com.nexmo.sdk.core.client.ClientBuilderException;
import com.nexmo.sdk.verify.client.VerifyClient;
import com.nexmo.sdk.verify.event.UserObject;
import com.nexmo.sdk.verify.event.VerifyClientListener;
import com.nexmo.sdk.verify.event.VerifyError;

import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.util.UUID;

import io.github.kobakei.grenade.annotation.Navigator;
import io.realm.Realm;
import otf.project.otf.R;
import otf.project.otf.activities.base.BaseActivity;
import otf.project.otf.fragments.VerificationCodeFragment;
import otf.project.otf.fragments.VerificationPhoneFragment;
import otf.project.otf.fragments.base.BaseFragment;
import otf.project.otf.messages.VerifyCodeMessage;
import otf.project.otf.messages.VerifyPhoneNumberMessage;
import otf.project.otf.models.OTFRole;
import otf.project.otf.models.OTFUser;
import otf.project.otf.utils.Constants;
import otf.project.otf.utils.RoleUtils;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

@Navigator
@RuntimePermissions
public class VerificationActivity extends BaseActivity implements VerifyClientListener {

    private Toolbar toolbar;

    private NexmoClient nexmoClient;
    private VerifyClient verifyClient;

    private ViewPager pager;

    private ProgressDialog loadingDialog;
    private String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        pager = (ViewPager) findViewById(R.id.pager);
        PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager(), new BaseFragment[] {
                new VerificationPhoneFragment()
        });
        pager.setAdapter(adapter);
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
            verifyClient.addVerifyListener(this);
        } catch (ClientBuilderException exc) {

        }
    }

    @NeedsPermission(Manifest.permission.READ_PHONE_STATE)
    public void performVerification(String phoneNumber) {
        loadingDialog = ProgressDialog.show(this, null, "Please, wait");
        verifyClient.getVerifiedUser(Constants.PHONE_COUNTRY_CODE, phoneNumber);
    }

    @Override
    public void onVerifyInProgress(VerifyClient verifyClient, UserObject user) {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }
        pager.setCurrentItem(1, true);
    }

    @Override
    public void onUserVerified(VerifyClient verifyClient, UserObject user) {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }
        final OTFUser otfUser = new OTFUser();
        otfUser.setId(UUID.randomUUID().toString());
        otfUser.setPhoneNumber(user.getPhoneNumber());
        otfUser.setName(name);

        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealm(otfUser);
            }
        });
        realm.close();

        RoleUtils.setRole(OTFRole.USER_CONFIRMED);

        startActivity(new ClientActivityNavigator().flags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .build(this));
    }

    @Override
    public void onError(VerifyClient verifyClient, VerifyError errorCode, UserObject user) {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }
    }

    @Override
    public void onException(IOException exception) {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }
    }

    @Subscribe
    public void verifyPhoneNumber(VerifyPhoneNumberMessage message) {
        name = message.getName();

        final OTFUser otfUser = new OTFUser();
        otfUser.setId(UUID.randomUUID().toString());
        otfUser.setPhoneNumber("" );
        otfUser.setName(name);

        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealm(otfUser);
            }
        });
        realm.close();

        RoleUtils.setRole(OTFRole.USER_CONFIRMED);

        startActivity(new ClientActivityNavigator().flags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .build(this));

//        VerificationActivityPermissionsDispatcher.performVerificationWithCheck(this, message.getPhoneNumber());
    }

    @Subscribe
    public void onVerifyCode(VerifyCodeMessage message) {
        loadingDialog = ProgressDialog.show(this, null, "Please, wait");
        verifyClient.checkPinCode(message.getCode());
    }

    private static class PagerAdapter extends FragmentPagerAdapter {

        private BaseFragment[] fragments;

        public PagerAdapter(FragmentManager fm, BaseFragment[] fragments) {
            super(fm);
            this.fragments = fragments;
        }

        @Override
        public int getCount() {
            return fragments.length;
        }

        @Override
        public Fragment getItem(int position) {
            return fragments[position];
        }
    }

}
