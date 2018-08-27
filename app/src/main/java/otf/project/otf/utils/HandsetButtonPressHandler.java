package otf.project.otf.utils;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;

/**
 * Created by denismalcev on 10.06.17.
 */

public class HandsetButtonPressHandler {

    public interface HandsetButtonListener {
        void onClick(int count);
    }

    private static final int MSG_PERFORM_ACTION = 1;

    private final HandsetButtonListener listener;
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_PERFORM_ACTION) {
                Log.d("OTF", "handle click count: " + clickCount);
                listener.onClick(clickCount); clickCount = 0;
            }
        }
    };

    private int clickCount = 0;

    public HandsetButtonPressHandler(HandsetButtonListener listener) {
        this.listener = listener;
    }

    private static final long clickTimeout = 700l;
    private long lastClickTimestamp = 0l;

    public void handleClick(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_UP) {
            if (lastClickTimestamp == 0) {
                lastClickTimestamp = System.currentTimeMillis();
            }

            handler.removeMessages(MSG_PERFORM_ACTION);
            clickCount++;
            handler.sendMessageDelayed(handler.obtainMessage(MSG_PERFORM_ACTION), clickTimeout);
            lastClickTimestamp = System.currentTimeMillis();
        }

    }
}
