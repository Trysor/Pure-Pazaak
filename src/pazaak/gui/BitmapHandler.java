package pazaak.gui;

import pazaak.android.MainGamePanel;
import pazaak.android.R;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class BitmapHandler {

	
	private Resources res;
	
	private final Double scale;
	
	private Bitmap[] bitmaps;
	
	private BitmapFactory.Options options;

	
	public BitmapHandler(MainGamePanel game) {
		this.res = game.getResources();
		this.scale = game.getScale();
	
		this.options = new BitmapFactory.Options();
		this.options.inPurgeable = true;
		this.options.inInputShareable = true;
		this.options.inDither = true;
		
		
		
		bitmaps = new Bitmap[19]; // hardcoding to reduce load
		bitmaps[0] =  getImage(R.drawable.background);
		bitmaps[1] =  getImage(R.drawable.button);
		bitmaps[2] =  getImage(R.drawable.buttonlong);
		bitmaps[3] =  getImage(R.drawable.cardbackground);
		bitmaps[4] =  getImage(R.drawable.carddealer);
		bitmaps[5] =  getImage(R.drawable.cardnegative);
		bitmaps[6] =  getImage(R.drawable.cardpositive);
		bitmaps[7] =  getImage(R.drawable.cardspecial);
		bitmaps[8] =  getImage(R.drawable.credits);
		bitmaps[9] =  getImage(R.drawable.instructions);
		bitmaps[10] = getImage(R.drawable.menu);
		bitmaps[11] = getImage(R.drawable.musicicon);
		bitmaps[12] = getImage(R.drawable.musicicondisabled);
		bitmaps[13] = getImage(R.drawable.notificationbox);
		bitmaps[14] = getImage(R.drawable.setlight);
		bitmaps[15] = getImage(R.drawable.soundicon);
		bitmaps[16] = getImage(R.drawable.soundicondisabled);
		bitmaps[17] = getImage(R.drawable.statistics);
		bitmaps[18] = getImage(R.drawable.story);
		
		this.res = null;
		this.options = null;
	}
	
	
	/* ---------------------------------------------------------------------
	 * --- Getters
	 * ---------------------------------------------------------------------
	 */
	
	
	public Bitmap getBackground() 				{ return bitmaps[0]; }
	public Bitmap getButton() 					{ return bitmaps[1]; }
	public Bitmap getButtonlong() 				{ return bitmaps[2]; }
	public Bitmap getCardbackground()			{ return bitmaps[3]; }
	public Bitmap getCarddealer()				{ return bitmaps[4]; } 
	public Bitmap getCardnegative() 			{ return bitmaps[5]; }
	public Bitmap getCardpositive() 			{ return bitmaps[4]; } // yeah
	public Bitmap getCardspecial() 				{ return bitmaps[7]; }
	public Bitmap getCredits() 					{ return bitmaps[8]; }
	public Bitmap getInstructions() 			{ return bitmaps[9]; }
	public Bitmap getMenu() 					{ return bitmaps[10]; }
	public Bitmap getMusicicon() 				{ return bitmaps[11]; }
	public Bitmap getMusicicondisabled() 		{ return bitmaps[12]; }
	public Bitmap getNotificationbox() 			{ return bitmaps[13]; }
	public Bitmap getSetlight() 				{ return bitmaps[14]; }
	public Bitmap getSoundicon() 				{ return bitmaps[15]; }
	public Bitmap getSoundicondisabled() 		{ return bitmaps[16]; }
	public Bitmap getStatistics() 				{ return bitmaps[17]; }
	public Bitmap getStory()					{ return bitmaps[18]; }
	
	public Bitmap getBitmapFromIndex(int index) {
		return bitmaps[index];
	}

	
	
	
	
	/* ---------------------------------------------------------------------
	 * --- Bitmap image methods
	 * ---------------------------------------------------------------------
	 */ 
	
	
	private Bitmap getImage(int id) {
	    Bitmap img = BitmapFactory.decodeResource(res, id, options );
	    // Bitmap.createScaledBitmap(img, (int)(img.getWidth()*scale), (int)(img.getHeight()*scale), true);
	    
	    return Bitmap.createScaledBitmap(img, (int)(img.getWidth()*scale), (int)(img.getHeight()*scale), true);
	}

	public Bitmap getScaledBitmap(int id) {
		return bitmaps[id];
	}
	
	

	/* ---------------------------------------------------------------------
	 * --- Animation methods
	 * ---------------------------------------------------------------------
	 */ 
	
	
	
	
	
}
