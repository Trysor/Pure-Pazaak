package pazaak.gui;

import pazaak.android.MainGamePanel;
import android.graphics.Bitmap;
import android.view.MotionEvent;


public class GUIToggle extends GUIObject {

	
	private boolean touched;	// if button is touched
	
	private Bitmap enabled;
	private Bitmap disabled;
	
	private MainGamePanel parent;
	private boolean toggledState;
	
	public GUIToggle(MainGamePanel parent, Bitmap enabled, Bitmap disabled, boolean toggledState, int x, int y) {
		super(x, y);
		
		this.parent = parent;
		
		this.enabled = enabled;
		this.disabled = disabled;
			
		this.toggledState = toggledState;
		super.setBitmap(toggledState ? enabled : disabled);
	}
	

	public boolean getToggledState() {
		return toggledState;
	}
	
	
	public boolean isTouched() {
		return touched;
	}
	
	public void handleTouch(boolean touched) {
		this.touched = touched;
	}
	
	public void setTouched(boolean touched, boolean isRelease) {
		if (this.touched && !touched && isRelease) {
			toggledState = !toggledState; // toggle!
			parent.handleToggle(this);
			super.setBitmap(toggledState ? enabled : disabled);
		}
		handleTouch(touched);
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
				handleTouch(true);
			} else {
				handleTouch(false);
			}
		} else {
			handleTouch(false);
		}

	}

	
}