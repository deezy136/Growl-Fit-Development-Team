package otf.project.otf.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;

import io.github.kobakei.grenade.annotation.Navigator;
import otf.project.otf.R;
import otf.project.otf.activities.base.BaseActivity;

/**
 * Created by denismalcev on 10.06.17.
 */

@Navigator
public class DebugActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activite_debug);
    }
}
