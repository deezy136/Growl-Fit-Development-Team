package otf.project.otf.utils;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import otf.project.otf.OTFApp;
import otf.project.otf.models.OTFRole;

/**
 * Created by denismalcev on 03.06.17.
 */

public class RoleUtils {

    public static OTFRole getRole() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(OTFApp.instance.getApplicationContext());
        int roleOrdinal = preferences.getInt("ROLE", 0);
        return OTFRole.values()[roleOrdinal];
    }

    public static void setRole(OTFRole role) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(OTFApp.instance.getApplicationContext());
        preferences.edit().putInt("ROLE", role.ordinal()).apply();
    }

}
