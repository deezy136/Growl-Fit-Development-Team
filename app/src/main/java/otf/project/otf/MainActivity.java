package otf.project.otf;

import android.Manifest;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import otf.project.otf.activities.ClientActivityNavigator;
import otf.project.otf.activities.ServerActivityNavigator;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;


@RuntimePermissions
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



    }

    public void startAsServer(View view) {
        MainActivityPermissionsDispatcher.proceedAsServerWithCheck(this);
    }

    public void startAsClient(View view) {
        startActivity(new ClientActivityNavigator().build(this));
    }

    @NeedsPermission(Manifest.permission.RECORD_AUDIO)
    public void proceedAsServer() {
        startActivity(new ServerActivityNavigator().build(this));
    }
}
