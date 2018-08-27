package otf.project.otf.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import otf.project.otf.R;
import otf.project.otf.activities.base.BaseActivity;
import otf.project.otf.models.OTFRole;
import otf.project.otf.utils.RoleUtils;

public class WelcomeGetStartActivity extends BaseActivity {

    private Button mButton;
    private EditText mEditText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_get_started);

        // This sets the view of the get started button to appear on the screen
        mButton = (Button) findViewById(R.id.b_get_started_button);

        //This calls the on clicke listener so that when get started is pressed it takes you to the choose role screen
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // This intent actuall takes you to the choose role screen
                Intent intent = new Intent(WelcomeGetStartActivity.this, ChooseRoleActivity.class);
                startActivity(intent);
            }
        });


    }
}
