package otf.project.otf.utils.voice;

import android.os.Process;

/**
 * Created by denis on 24.02.2018.
 */

public class AudioRecorder implements Runnable {

    @Override
    public void run() {
        android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);

    }
}
