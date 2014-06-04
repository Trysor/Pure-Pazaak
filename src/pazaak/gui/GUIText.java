package pazaak.gui;

import pazaak.android.MainGamePanel;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;


public class GUIText {
	
	
	private String text;
	private int textSize;
	private int x;			// the X coordinate
	private int y;			// the Y coordinate
	
	private Paint paint;
	ValueAnimator colorAnimation;

	
	public GUIText(MainGamePanel parent, int x, int y, int textSize, Paint.Align align) {
		this.x = x;
		this.y = y;
		
		this.textSize = textSize;
		
		
		paint = new Paint();
		paint.setTypeface(parent.font);
		paint.setAntiAlias(true);
		paint.setTextAlign(align);
		paint.setShadowLayer((float)(1), 2, 2, Color.BLACK);
		paint.setColor(Color.LTGRAY);
		paint.setStyle(Style.FILL);
		paint.setTextSize(textSize);
	
		
		this.colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), Color.LTGRAY, Color.GREEN);
		this.colorAnimation.addUpdateListener(new AnimatorUpdateListener() {

		    @Override
		    public void onAnimationUpdate(ValueAnimator animator) {
		    	paint.setColor((Integer)animator.getAnimatedValue());
		    }
		});
		this.colorAnimation.setRepeatCount(ValueAnimator.INFINITE);
		this.colorAnimation.setRepeatMode(ValueAnimator.REVERSE);
		
		
		
	}
	
	public String getText() {
		return text;
	}

	public int getX() {
		return x;
	}
	public int getY() {
		return y;
	}	
	
	public void setText(String text) {
		this.text = text;
	}
	public void setX(int x) {
		this.x = x;
	}
	public void setY(int y) {
		this.y = y;
	}
	
	
	private void enableAnimation(boolean enabled) {
		if (enabled) {
			colorAnimation.start();
		} else {
			colorAnimation.end();
		}
	}
	
	
	public void setTextMode(String s) {
		enableAnimation(false);
		
		if (s.equals("stand")) {
			paint.setColor(Color.YELLOW);
			
		} else if (s.equals("animate")) {
			enableAnimation(true);
			
		} else if (s.equals("normal")) {
			paint.setColor(Color.LTGRAY);
			
		} else if (s.equals("red")) {
			paint.setColor(Color.RED);
		}
	}
	


	
	public void draw(Canvas canvas) {
		canvas.drawText(text , x, y - (textSize/2), paint);
	}


}