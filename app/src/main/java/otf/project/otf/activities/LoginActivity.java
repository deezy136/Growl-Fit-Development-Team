package otf.project.otf.activities;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import otf.project.otf.R;
import otf.project.otf.models.OTFRole;
import otf.project.otf.utils.RoleUtils;

public class LoginActivity extends AppCompatActivity {

    private Button mSignInButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sigin_in_screen);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSignInButton = (Button) findViewById(R.id.button_sign);
        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OTFRole currentRole = RoleUtils.getRole();
                navigateWithRole(currentRole);
            }
        });




    }
    private void navigateWithRole(OTFRole role) {
        switch (role) {
            case INSTRUCTOR:
                startActivity(new GroupActivityNavigator().build(this));
                break;
            case USER_CONFIRMED:
                startActivity(new ClientActivityNavigator().build(this));
                break;
            case USER:
                break;
            case NONE:
                startActivity(new ChooseRoleActivityNavigator().build(this));
                break;
        }
    }

}
