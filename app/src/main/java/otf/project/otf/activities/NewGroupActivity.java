package otf.project.otf.activities;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import java.util.UUID;

import io.github.kobakei.grenade.annotation.Navigator;
import io.realm.Realm;
import otf.project.otf.R;
import otf.project.otf.activities.base.BaseActivity;
import otf.project.otf.models.OTFUser;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

/**
 * Created by denismalcev on 04.06.17.
 */

@Navigator
@RuntimePermissions
public class NewGroupActivity extends BaseActivity {

    private Toolbar toolbar;
    private EditText editText;
    private ImageButton verificationButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_create);

        editText = (EditText) findViewById(R.id.user_name_field);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                verificationButton.setEnabled(editText.getText().toString().length() > 0);
            }
        });
        verificationButton = (ImageButton) findViewById(R.id.verification_button);
        verificationButton.setEnabled(false);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setTitle("New group");
    }


    public void proceed(View view) {
        NewGroupActivityPermissionsDispatcher.proceedGroupCreationWithCheck(this);
    }

    @NeedsPermission(Manifest.permission.RECORD_AUDIO)
    public void proceedGroupCreation() {
        String userName = editText.getText().toString();
        final OTFUser user = new OTFUser();
        user.setId(UUID.randomUUID().toString());
        user.setName(userName);
        user.setInstructor(true);
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealm(user);
            }
        });
        realm.close();

        startActivity(new GroupActivityNavigator().flags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK).build(this));
    }
}
