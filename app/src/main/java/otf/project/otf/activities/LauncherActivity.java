package otf.project.otf.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;

import otf.project.otf.R;
import otf.project.otf.models.OTFRole;
import otf.project.otf.utils.RoleUtils;

/**
 * Created by denismalcev on 03.06.17.
 */

public class LauncherActivity extends Activity {

    // This is the timer for the splash screen its currently set at 2 seconds
    private static int SPLASH_TIME_OUT = 2000;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //This sets the actual layout of the splash screen
        setContentView(R.layout.activity_splash_screen);

        // This is where the splash screen is handled
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                //This intent will take you to the Get started page after the 2 second timer
                Intent intent = new Intent(LauncherActivity.this, WelcomeGetStartActivity.class);
                startActivity(intent);
            }
        }, SPLASH_TIME_OUT);


    }


}
