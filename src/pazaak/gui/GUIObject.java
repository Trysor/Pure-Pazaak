package pazaak.gui;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;


public abstract class GUIObject {
	protected final static Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
	

	private Bitmap bitmap;	// the current bitmap
	private int x;			// the X coordinate
	private int y;			// the Y coordinate

	
	public GUIObject(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public Bitmap getBitmap() {
		return bitmap;
	}
	public int getX() {
		return x;
	}
	public int getY() {
		return y;
	}
	
	
	
	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}
	public void setX(int x) {
		this.x = x;
	}
	public void setY(int y) {
		this.y = y;
	}

	
	public void draw(Canvas canvas) {
		canvas.drawBitmap(bitmap, x - (bitmap.getWidth() / 2), y - (bitmap.getHeight() / 2), paint);
		//canvas.drawBitmap(bitmap, x, y, null);
	}


}