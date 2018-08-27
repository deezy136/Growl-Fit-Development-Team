package otf.project.otf.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;

import io.github.kobakei.grenade.annotation.Navigator;
import otf.project.otf.R;
import otf.project.otf.activities.base.BaseActivity;

/**
 * Created by denismalcev on 03.06.17.
 */

@Navigator
public class ChooseGroupActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_group);
    }
}
