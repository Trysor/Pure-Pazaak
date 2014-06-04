package pazaak.gui;

import pazaak.android.MainGamePanel;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;


public class GUIButton extends GUIObject {

	
	private boolean touched;	// if button is touched
	
	private final Bitmap bitmap;
	private final GUIText text;

	private final MainGamePanel game;
	
	//private final int textOffsetX;
	private final int textOffsetY;
	
	public GUIButton(MainGamePanel game, Bitmap bitmap, String str, int x, int y) {
		super(x, y);
		this.game = game;
		
		this.bitmap = bitmap;
		super.setBitmap(bitmap);
		
		this.textOffsetY = y+(int)(25*game.getScale());
		
		
		// GUIText(int x, int y, int textSize, boolean centerText)
		this.text = new GUIText(game, x, this.textOffsetY, (int)(35*game.getScale()), Paint.Align.CENTER);
		this.text.setText(str);
	}
	

	
	public boolean isTouched() {
		return touched;
	}
	
	public void setTouched(boolean touched) {
		this.touched = touched;
		this.text.setY(touched ? (this.textOffsetY+(int)(5*game.getScale())) : this.textOffsetY);
		 // super.setBitmap(touched ? down : bitmap); <------------- MOVE THE TEXT
	}
	
	public void setTouched(boolean touched, boolean isRelease) {
		if (this.touched && !touched && isRelease) {
			game.handleClicks(this);
		}
		setTouched(touched);
	}
	
	@SuppressWarnings("static-access")
	@Override
	public void draw(Canvas canvas) {
		canvas.drawBitmap(bitmap, super.getX() - (bitmap.getWidth() / 2), super.getY() - (bitmap.getHeight() / 2), super.paint);
		this.text.draw(canvas);
	}
	
	
	/**
	 * Handles the {@link MotionEvent.ACTION_DOWN} event. If the event happens on the 
	 * bitmap surface then the touched state is set to <code>true</code> otherwise to <code>false</code>
	 * @param eventX - the event's X coordinate
	 * @param eventY - the event's Y coordinate
	 */
	public void handleActionDown(int eventX, int eventY) {
		Bitmap bm = super.getBitmap();
		if (eventX >= (super.getX() - bm.getWidth() / 2) && (eventX <= (super.getX() + bm.getWidth()/2))) {
			if (eventY >= (super.getY() - bm.getHeight() / 2) && (eventY <= (super.getY() +bm.getHeight() / 2))) {
				setTouched(true);
			} else {
				setTouched(false);
			}
		} else {
			setTouched(false);
		}

	}

	
}