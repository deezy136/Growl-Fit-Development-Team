package otf.project.otf.utils;

import otf.project.otf.BuildConfig;

/**
 * Created by denismalcev on 03.06.17.
 */

public class Constants {

    public static final String APPLICATION_KEY = "7aabe422-4f46-43f2-8eb7-877b24bc0f28";

    public static final String NEXMO_APP_ID = "2d649a6e-e90f-4b7c-9511-5fb057fe3d9b";
    public static final String NEXMO_APP_SECRET = "7d2f524619a1fdd";

    public static final int SERVER_PORT = 65535;
    public static final int PENDING_INVITATION_PORT = 65534;

    public static final String SERVICE_UUID_KEY = "uuid";
    public static final String SERVICE_START_TIME = "start_time";
    public static final String HTTP_PORT = "http_port";
    public static final String SOCKET_PORT = "socket_port";

    public static final String SOCKET_RECEIVER_PORT = "socket_receiver_port";
    public static final String CLIENT_GROUP_ID = "client_group_id";

    public static final String PHONE_COUNTRY_CODE = BuildConfig.COUNTRY_CODE;

    public static class AudioConfig {
        public static final byte AUDIO_PACKAGE = Byte.MIN_VALUE;
        public static final byte DATA_PACKAGE = Byte.MAX_VALUE;
        public static final int SAMPLE_RATE = 8000;
        public static final int SAMPLE_INTERVAL = 5;
        public static final int SAMPLE_SIZE = 2;
        public static final int BUF_SIZE = SAMPLE_INTERVAL * SAMPLE_INTERVAL * SAMPLE_SIZE * 2;
    }

    public static final String PREF_SHOWED_HINT_1 = "pref_showed_hint_1";
    public static final String PREF_SHOWED_HINT_2 = "pref_showed_hint_2";
}
