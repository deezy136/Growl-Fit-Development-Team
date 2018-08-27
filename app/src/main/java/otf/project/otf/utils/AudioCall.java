package otf.project.otf.utils;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Process;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;

import otf.project.otf.OTFApp;

import static otf.project.otf.utils.Constants.AudioConfig.BUF_SIZE;
import static otf.project.otf.utils.Constants.AudioConfig.SAMPLE_INTERVAL;
import static otf.project.otf.utils.Constants.AudioConfig.SAMPLE_RATE;

public class AudioCall implements AudioManager.OnAudioFocusChangeListener {

	private boolean mic = false;
	private boolean speakers = false;

	private AudioManager audioManager;

	private List<InetSocketAddress> clientList;
	private boolean source = false;

	public AudioCall(boolean source, List<InetSocketAddress> clientList) {
		this.source = source;
		this.clientList = clientList;
	}

	public void startCall() {
		startMic();
	}
	
	public void endCall() {
		muteMic();
		muteSpeakers();
	}
	
	public void muteMic() {
		mic = false;
		audioManager.abandonAudioFocus(this);
	}
	
	public void muteSpeakers() {
		speakers = false;
	}

	private static int[] mSampleRates = new int[] { 8000, 11025, 22050, 44100 };

	public AudioRecord findAudioRecord() {
		for (int rate : mSampleRates) {
			for (short audioFormat : new short[] { AudioFormat.ENCODING_PCM_8BIT, AudioFormat.ENCODING_PCM_16BIT }) {
				for (short channelConfig : new short[] { AudioFormat.CHANNEL_IN_MONO, AudioFormat.CHANNEL_IN_STEREO }) {
					try {
						int bufferSize = AudioRecord.getMinBufferSize(rate, channelConfig, audioFormat);

						if (bufferSize != AudioRecord.ERROR_BAD_VALUE) {
							// check if we can instantiate and have a success
							AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, rate, channelConfig, audioFormat, bufferSize);

							if (recorder.getState() == AudioRecord.STATE_INITIALIZED)
								return recorder;
						}
					} catch (Exception e) {

					}
				}
			}
		}
		return null;
	}
	
	public void startMic() {
		mic = true;

		audioManager = (AudioManager) OTFApp.instance.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
		audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);

		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
				AudioRecord audioRecorder = new AudioRecord (MediaRecorder.AudioSource.MIC, SAMPLE_RATE,
						AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
						AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT));
				int bytes_read = 0;
				int bytes_sent = 0;
				byte[] buf = new byte[BUF_SIZE + 1];
				try {

					DatagramSocket socket = new DatagramSocket();
					audioRecorder.startRecording();
					while(mic) {
						bytes_read = audioRecorder.read(buf, 1, BUF_SIZE);
						buf[0] = Constants.AudioConfig.AUDIO_PACKAGE;
						for (InetSocketAddress address : clientList) {
							DatagramPacket packet = new DatagramPacket(buf, bytes_read, address.getAddress(), address.getPort());
							socket.send(packet);
						}
						bytes_sent += bytes_read;
						Thread.sleep(SAMPLE_INTERVAL, 0);
					}

					audioRecorder.stop();
					audioRecorder.release();
					socket.disconnect();
					socket.close();
					mic = false;
					return;
				} catch(InterruptedException e) {
					mic = false;
				} catch(SocketException e) {
					mic = false;
				} catch(UnknownHostException e) {
					mic = false;
				} catch(IOException e) {
					mic = false;
				}
			}
		});
		thread.start();
	}

	public List<InetSocketAddress> getClientList() {
		return clientList;
	}

	@Override
	public void onAudioFocusChange(int focusChange) {

	}
}