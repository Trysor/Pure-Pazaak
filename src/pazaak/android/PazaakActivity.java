package pazaak.android;

import pazaak.game.Statistics;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

public class PazaakActivity extends Activity {
    /** Called when the activity is first created. */
	
	private static final String TAG = PazaakActivity.class.getSimpleName();
	
	private GameSound sound;
	
	private SharedPreferences savedVars;
	private Statistics stats;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // requesting to turn the title OFF
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // making it full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        savedVars = getSharedPreferences("Pazaak", 0);
        sound = new GameSound(this, savedVars);
        stats = new Statistics(savedVars);
        
        MainGamePanel game = new MainGamePanel(this, sound, stats);
        
        setContentView(game);
        Log.d(TAG, "View added");
        
        
        sound.prepareMusic();
        
	    super.onCreate(savedInstanceState);	    
    }
    
	@Override
	protected void onDestroy() {
		Log.d(TAG, "Destroying...");
		sound.releaseMusic();
		stats.registerForfeit();
		System.gc();
		super.onDestroy();
	}

	@Override
	protected void onStop() {
		Log.d(TAG, "Stopping...");
		sound.pauseMusic();
		System.gc();
		super.onStop();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		sound.resumeMusic();
	}
	
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	@Override
	public void onTrimMemory(int level) {
		if (level == TRIM_MEMORY_UI_HIDDEN) {
			sound.pauseMusic();
		}
		super.onTrimMemory(level);
	}
    
}