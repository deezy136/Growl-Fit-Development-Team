package otf.project.otf.utils;

import android.content.IntentFilter;

/**
 * Created by denismalcev on 28.05.17.
 */

public class IntentFilterCreator {

    public static IntentFilter create(String... actions) {
        IntentFilter intentFilter = new IntentFilter();
        for (String action : actions) {
            intentFilter.addAction(action);
        }
        return intentFilter;
    }

}
