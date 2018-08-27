package otf.project.otf.utils;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Process;

import otf.project.otf.OTFApp;
import otf.project.otf.networking.udp.Server;

import static otf.project.otf.utils.Constants.AudioConfig.BUF_SIZE;
import static otf.project.otf.utils.Constants.AudioConfig.SAMPLE_RATE;

/**
 * Created by denismalcev on 10.06.17.
 */

public class AudioReceiver implements Server.OnDataCallback, AudioManager.OnAudioFocusChangeListener {

    private boolean speakers = false;
    private AudioManager audioManager;

    private AudioTrack track;

    public void startSpeakers() {
        if (!speakers) {

            audioManager = (AudioManager) OTFApp.instance.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
            audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);

            speakers = true;
            Thread receiveThread = new Thread(new Runnable() {

                @Override
                public void run() {
                    Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
                    track = new AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE,
                            AudioFormat.CHANNEL_OUT_MONO,
                            AudioFormat.ENCODING_PCM_16BIT, BUF_SIZE,
                            AudioTrack.MODE_STREAM);

                    track.play();

                    while(speakers) {}

                    track.stop();
                    track.flush();
                    track.release();
                    speakers = false;
                    return;
                }
            });

            receiveThread.start();
        }
    }


    public void stopSpeakers() {
        speakers = false;
        audioManager.abandonAudioFocus(this);
    }

    @Override
    public void onDataReceived(byte[] data) {
        if (track != null) {
            track.write(data, 1, data.length - 1);
        }
    }

    @Override
    public void onAudioFocusChange(int focusChange) {

    }
}
