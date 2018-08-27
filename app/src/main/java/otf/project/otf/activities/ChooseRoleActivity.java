package otf.project.otf.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.net.URISyntaxException;

import io.github.kobakei.grenade.annotation.Navigator;
import otf.project.otf.R;
import otf.project.otf.activities.base.BaseActivity;

/**
 * Created by denismalcev on 03.06.17.
 */

@Navigator
public class ChooseRoleActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_role);
    }

    public void createNewGroup(View view) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            new AlertDialog.Builder(this).setTitle("Warning")
                    .setMessage(R.string.coach_mode_warning)
                    .setPositiveButton(android.R.string.ok, null)
                    .create().show();
        } else {
            startActivity(new NewGroupActivityNavigator().build(this));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.dash_board_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    public void joinExistingGroup(View view) {
        startActivity(new VerificationActivityNavigator().build(this));
    }


}
