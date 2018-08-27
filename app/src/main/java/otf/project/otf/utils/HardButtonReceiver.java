package otf.project.otf.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

/**
 * Created by denismalcev on 08.06.17.
 */

public class HardButtonReceiver extends BroadcastReceiver {

    private final HardButtonListener listener;

    public HardButtonReceiver(HardButtonListener listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        KeyEvent key = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);

        if (key.getAction() == KeyEvent.ACTION_UP)  {
            int keycode = key.getKeyCode();

            if(keycode == KeyEvent.KEYCODE_MEDIA_NEXT)  {
                listener.onNextButtonPress();
            } else if(keycode == KeyEvent.KEYCODE_MEDIA_PREVIOUS) {
                listener.onPrevButtonPress();
            }  else if(keycode == KeyEvent.KEYCODE_HEADSETHOOK)  {
                listener.onPlayPauseButtonPress();
            }
        }
    }

    public interface HardButtonListener {
        void onPrevButtonPress();
        void onNextButtonPress();
        void onPlayPauseButtonPress();
    }
}
