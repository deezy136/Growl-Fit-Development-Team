package otf.project.otf.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Created by denismalcev on 08.06.17.
 */

public final class GsonManager {

    private final Gson gson;

    private static final GsonManager instance = new GsonManager();

    public static GsonManager getInstance() {
        return instance;
    }

    protected GsonManager() {
        gson = new GsonBuilder().create();
    }

    public Gson getGson() {
        return gson;
    }
}
