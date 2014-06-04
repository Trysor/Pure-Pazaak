package pazaak.gui;

import pazaak.android.MainGamePanel;
import pazaak.game.Card;
import pazaak.game.HandCard;
import pazaak.game.PazaakGame;
import pazaak.game.Player;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;


public class GUICard extends GUIObject {

	
	private boolean touched;	// if button is touched
	
	
	private Card card;
	
	private final MainGamePanel parent;
	
	private final GUIText textValue;
	private final GUIText textType;
	
	
	private final int origX;
	private final int origY;
	
	private int x;
	private int y;
	
	private final int cardNumber;
	private final int playerID;
	private final boolean isHand;


	private boolean dragged;
	
	public GUICard(MainGamePanel parent, boolean isHand, int playerID, int cardNumber, int x, int y) {
		super(x, y);
		
		this.parent = parent;
		
		this.origX = x;
		this.x = x;
		this.origY = y;
		this.y = y;
		
		this.cardNumber = cardNumber; // id = card number (from 1 to 9 or from 1 to 4)
		this.playerID = playerID;
		this.isHand = isHand;
		
		this.card = null; // for now.
		
		// GUIText(int x, int y, int textSize, boolean centerText)
		this.textValue = new GUIText(parent, x, y, (int)(35*parent.getScale()), Paint.Align.CENTER);
		this.textValue.setText("");
		
		this.textType = new GUIText(parent, x, y+(int)(40*parent.getScale()), (int)(22*parent.getScale()), Paint.Align.CENTER);
		this.textType.setText("");
		
	}
	
	
	public void setX(int x) {	
		int minX = (int) (parent.getWidth()/2 - (parent.getWidth()/2) + super.getBitmap().getWidth()/2);
		int maxX = (int) (parent.getWidth()/2 + (parent.getWidth()/2) - super.getBitmap().getWidth()/2);

		x = minX < x ? x : minX;
		x = maxX > x ? x : maxX;
		
		this.x = x;
		super.setX(x);
		this.textValue.setX(x);
		this.textType.setX(x);
	}
	public void setY(int y) {
		int minY = (int) (parent.getHeight()/2 - (parent.getGameHeight()/2) + super.getBitmap().getHeight()/2);
		int maxY = (int) (parent.getHeight()/2 + (parent.getGameHeight()/2) - super.getBitmap().getHeight()/2);
		
		y = minY < y ? y : minY;
		y = maxY > y ? y : maxY;
		
		this.y = y;
		super.setY(y);
		this.textValue.setY(y);
		this.textType.setY(y+(int)(40*parent.getScale()));
	}
	
	public void resetPos() {
		setX(this.origX);
		setY(this.origY);
	}
	
	public int getCardNumber() {
		return this.cardNumber;
	}

	
	public void setCard(Card c) {
		this.card = c;
		if (c != null) { // we sometimes erase cards too
			setCardGraphics();
		}
		
	}
	

	public void setCardGraphics() {
		if (this.card == null) {
			return;
		}
		
		PazaakGame game = parent.getGame();
		int pTurn = game.getPlayerTurn();
		
		boolean showFace = true;
		
		if (this.isHand) { // it is a handcard.
			
			if (pTurn == this.playerID) { // it is our turn
				if (game.getPlayer(pTurn).isControlledByAI()) { // though we are an AI. never show AI cards.
					showFace = false;					
				}
			} else { // it isn't even our turn... buuut
				if (!game.getPlayer(pTurn).isControlledByAI()) { // we play against an AI, so we can show them anyways
					showFace = false; 
				}
			}
		}
		
		if (!showFace) {
			super.setBitmap(parent.getBitmapHandler().getCardbackground());
			this.textType.setText("");
			this.textValue.setText("");
			return;
		}

		this.textValue.setText(String.valueOf(this.card.getValue()));
		boolean isPos = this.card.getValue() > 0 ? true : false;
		// TIEBREAKER, FLIPPABLE, THREE_AND_SIX, TWO_AND_FOUR, NORMAL, DEALER;
		switch (this.card.getEnumType()) {
		case TIEBREAKER:
			super.setBitmap(parent.getBitmapHandler().getCardspecial());
			this.textType.setText("Tie");
			break;
		case DEALER:
			if (isPos) {
				super.setBitmap(parent.getBitmapHandler().getCardpositive()); // can be flipped
			} else {
				super.setBitmap(parent.getBitmapHandler().getCardnegative()); // can be flipped
			}
			this.textType.setText("");
			break;
		case FLIPPABLE:
			super.setBitmap(parent.getBitmapHandler().getCardspecial());
			this.textType.setText("Flip");
			break;
		case NORMAL:
			if (isPos) {
				super.setBitmap(parent.getBitmapHandler().getCardpositive()); // can be flipped
			} else {
				super.setBitmap(parent.getBitmapHandler().getCardnegative()); // can be flipped
			}
			this.textType.setText("");
			break;
		case THREE_AND_SIX:
			super.setBitmap(parent.getBitmapHandler().getCardspecial());
			this.textType.setText("3 & 6");
			break;
		case TWO_AND_FOUR:
			super.setBitmap(parent.getBitmapHandler().getCardspecial());
			this.textType.setText("2 & 4");
			break;
		case DOUBLE:
			super.setBitmap(parent.getBitmapHandler().getCardspecial());
			this.textType.setText("Double");
			break;
		default:
			super.setBitmap(parent.getBitmapHandler().getCardbackground());
			this.textType.setText("");
			break;
		}
	}
	
	
	
	
	
	public Card getCard() {
		return this.card;
	}
	
	public boolean hasCard() {
		if (this.card == null) {
			return false;
		}
		
		if (card instanceof HandCard && this.isHand) {
			return !((HandCard) card).isUsed();
		}
		
		return true;
	}
	

	
	public boolean isTouched() {
		return touched;
	}
	
	public void setDragged(boolean dragged) {
		this.dragged = dragged;
	}
	public void setTouched(boolean touched) {
		this.touched = touched;
	}
	
	
	
	
	
	
	@SuppressWarnings("static-access")
	@Override
	public void draw(Canvas canvas) {
		Bitmap bm = super.getBitmap();
		canvas.drawBitmap(bm, x - (bm.getWidth() / 2), y - (bm.getHeight() / 2), super.paint);
		this.textValue.draw(canvas);
		this.textType.draw(canvas);
	}
	
	
	
	private boolean playerCanPerformAction() {
		int playerTurn = parent.getGame().getPlayerTurn();
		Player player = parent.getGame().getPlayer(playerTurn);
		
		if (player.isControlledByAI() || // if the player is an AI
			playerTurn != this.playerID || // or the card doesn't belong to current turn's player's hand
			!this.isHand || // or the fact that this isn't even a card belonging to a hand
			player.hasCompletedRound() || player.hasCompletedSet() || // or the fact that the player has already done something
			player.getStack().size() == PazaakGame.CARD_LIMIT || // or that we've already filled 9 slots
			(card instanceof HandCard && ((HandCard) card).isUsed()) || // or that the card has already been used
			!(parent.getGamePlayingState() == 1) ) { // or that we aren't even playing atm..
				
			return false; // should make us unable to use another card.
		}
		return true;
	}
	
	
	
	
	/**
	 * Handles the {@link MotionEvent.ACTION_DOWN} event. If the event happens on the 
	 * bitmap surface then the touched state is set to <code>true</code> otherwise to <code>false</code>
	 * @param eventX - the event's X coordinate
	 * @param eventY - the event's Y coordinate
	 */
	public void handleActionDown(int eventX, int eventY) {
		if (!playerCanPerformAction()) {
			return;
		}
		
		Bitmap bm = super.getBitmap();
		if (eventX >= (super.getX() - bm.getWidth() / 2) && (eventX <= (super.getX() + bm.getWidth()/2))) {
			if (eventY >= (super.getY() - bm.getHeight() / 2) && (eventY <= (super.getY() +bm.getHeight() / 2))) {
				this.touched = true;
				return;
			}
		}
	}
	
	public boolean handleActionMove(int eventX, int eventY) {
		if (!playerCanPerformAction()) {
			return false;
		}
		
		if (this.touched) { // we know this to be the only touched object
			Bitmap bm = super.getBitmap();
			int width = bm.getWidth();
			int height = bm.getHeight();
			
			int origLeft = this.origX - (width / 2);
			int origRight = this.origX + (width / 2);
			int origTop = this.origY - (height / 2);
			int origBottom = this.origY + (height / 2);
			

			if (((eventX >= origRight) || (eventX <= origLeft)) || ((eventY >= origBottom) || (eventY <=origTop))  ) { 
				this.setX(eventX);
				this.setY(eventY);
				this.dragged = true;
				return true;
			}
			this.dragged = false;
			this.setX(this.origX);
			this.setY(this.origY);
		}
		return false;
	}
	
	
	

	public void handleActionUp(int eventX, int eventY) {
		if (!playerCanPerformAction()) {
			return;
		}
		
		
		if (this.touched == false) { return; } // this card wasn't being touched anyways, so nothing to bother about		
		Bitmap bm = super.getBitmap();
		int width = bm.getWidth();
		int height = bm.getHeight();
		
		
		if (eventX >= (super.getX() - width / 2) && eventX <= (super.getX() + width/2) && // make sure we release with the card
			eventY >= (super.getY() - height / 2) && eventY <= (super.getY() + height / 2)) { // still beneath our finger
			if (this.dragged) {
				
				// check if we are far enough into game field to actively use the card
				if (this.y < (this.origY - height*1.2)) { // been moved and within "use" area
					parent.handleCardMoves(this);
				} else { // we were not. Lets return the card to the origPos.
					setX(this.origX);
					setY(this.origY);
				}
			} else {
				parent.handleCardClicks(this);
				setCardGraphics();
			}
			this.touched = false;
			this.dragged = false;
			return;
		}
		
		this.touched = false;
		this.dragged = false;
		setX(this.origX);
		setY(this.origY);
	}

	
}