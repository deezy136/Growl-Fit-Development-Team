package otf.project.otf.activities.base;

import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import otf.project.otf.messages.OTFSocketMessage;

/**
 * Created by denismalcev on 03.06.17.
 */

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSocketMessage(OTFSocketMessage message) {
        Toast.makeText(this, "Socket message: " + message.getMessage(), Toast.LENGTH_LONG).show();
    }
}
