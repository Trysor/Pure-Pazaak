package pazaak.android;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;

public class GameSound {
	
	
	private SoundPool sp;
	private AudioManager audioManager;
	
	private MediaPlayer musicPlayer;
	
	private HashMap<String, HashMap<String, Integer>> sounds;
	
	private final SharedPreferences savedVars;
	private final SharedPreferences.Editor savedVarsEditor;
	
	private final PazaakActivity activity;
	
	private boolean musicLoaded;
	
	
	public GameSound(PazaakActivity pazaak, SharedPreferences sv) {
		this.activity = pazaak;
		this.savedVars = sv;
		this.savedVarsEditor = savedVars.edit();
		activity.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		
		musicLoaded = false;
		
		sp = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
		audioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE); 
					
		sounds = new HashMap<String, HashMap<String, Integer>>();
					
		sp.setOnLoadCompleteListener(new OnLoadCompleteListener() {
		@Override
		public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
			for (Entry<String, HashMap<String, Integer>> entry : sounds.entrySet()) {
				if (entry.getValue().get("sound") == sampleId) {
					sounds.get(entry.getKey()).put("loaded", 1);
					break;
				}
			}
		}
		});
					
		sounds.put("button", new HashMap<String, Integer>());
		sounds.get("button").put("id", R.raw.button);
		sounds.get("button").put("loaded", 0);
		sounds.get("button").put("sound", sp.load(activity, R.raw.button, 1));
						
		sounds.put("dealercard", new HashMap<String, Integer>());
		sounds.get("dealercard").put("id", R.raw.dealercard);
		sounds.get("dealercard").put("loaded", 0);
		sounds.get("dealercard").put("sound", sp.load(activity, R.raw.dealercard, 1));
						
		sounds.put("draw", new HashMap<String, Integer>());
		sounds.get("draw").put("id", R.raw.draw);
		sounds.get("draw").put("loaded", 0);
		sounds.get("draw").put("sound", sp.load(activity, R.raw.draw, 1));
						
		sounds.put("handcard", new HashMap<String, Integer>());
		sounds.get("handcard").put("id", R.raw.handcard);
		sounds.get("handcard").put("loaded", 0);
		sounds.get("handcard").put("sound", sp.load(activity, R.raw.handcard, 1));
					
		sounds.put("match", new HashMap<String, Integer>());
		sounds.get("match").put("id", R.raw.match);
		sounds.get("match").put("loaded", 0);
		sounds.get("match").put("sound", sp.load(activity, R.raw.match, 1));		
					
		sounds.put("set", new HashMap<String, Integer>());
		sounds.get("set").put("id", R.raw.set);
		sounds.get("set").put("loaded", 0);
		sounds.get("set").put("sound", sp.load(activity, R.raw.set, 1));
					

					
		musicPlayer = MediaPlayer.create(activity, R.raw.music);
		musicPlayer.setLooping(true);
					
		musicPlayer.setOnPreparedListener(new OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer arg0) {
				musicLoaded = true;
				playMusic();
			}
		});
	}
	
	
	public boolean isMusicEnabled() {
		return savedVars.getBoolean("enableMusic", true);
	}
	
	public boolean isSoundEnabled() {
		return savedVars.getBoolean("enableSound", true);
	}
	
	public void enableMusic(boolean state) {
		savedVarsEditor.putBoolean("enableMusic", state).commit();
		if (state) {
			resumeMusic();
		} else {
			pauseMusic();
		}
		
	}
	
	public void enableSound(boolean state) {
		savedVarsEditor.putBoolean("enableSound", state).commit();
	}
	
	
	
	public void playSound(String s) {
		if (sounds.get(s) == null || (!savedVars.getBoolean("enableSound", true))) {
			return;
		}
		
        if (sounds.get(s).get("loaded").equals(1)) {
			// Getting the user sound settings
			float actualVolume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
			float maxVolume = (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
			float volume = actualVolume / maxVolume;

        	sp.play(sounds.get(s).get("sound"), volume, volume, 1, 0, 1f);
        }
	}
	
	public void playMusic() {
		if (!savedVars.getBoolean("enableMusic", true) || !musicLoaded) {
			return;
		}
		
		// Getting the user sound settings
		float actualVolume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		float maxVolume = (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		float volume = actualVolume / maxVolume;
		
		musicPlayer.setVolume(volume, volume);
		musicPlayer.start();
	}
	
	public void pauseMusic() {
		musicPlayer.pause();
	}
	
	
	public void prepareMusic() {
		try {
			musicPlayer.prepare();
		} catch (IllegalStateException e) {
		} catch (IOException e) {
		}
	}
	
	public void releaseMusic() {
		musicPlayer.release();
	}
	
	public void resumeMusic() {
		if (isMusicEnabled() && musicLoaded && !musicPlayer.isPlaying()) {
			float actualVolume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
			float maxVolume = (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
			float volume = actualVolume / maxVolume;
			musicPlayer.setVolume(volume, volume);
			
			//musicPlayer.seekTo(pausePoint);
			musicPlayer.start();
		}
	}
}
