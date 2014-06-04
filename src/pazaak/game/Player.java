package pazaak.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Player {


	/* ---------------------------------------------------------------------
	 * --- Vars
	 * ---------------------------------------------------------------------
	 */ 

	private int setsWon;
	
	private final List<HandCard> hand;
	private final List<Card> stack;
	
	private final String playerName;
	
	private final PazaakGame parentGame;
	private final int id;
	
	private boolean playedTiebreaker;

	private boolean completedDeal;
	private boolean completedRound;
	private boolean completedSet;

	private boolean controlledByAI;
	
	
	/* ---------------------------------------------------------------------
	 * --- Constructor
	 * ---------------------------------------------------------------------
	 */ 
	
	public Player(PazaakGame parentGame, int id, String playerName) {
		this.parentGame = parentGame;
		this.id = id;
		this.playerName = playerName;
		
		controlledByAI = false;
		
		hand = new ArrayList<HandCard>();
		for (int i = 0; i < 4; i++) {
			hand.add(parentGame.createHandCard());
		}
		setsWon = 0;
		stack = new ArrayList<Card>();
		playedTiebreaker = false;

		completedDeal = false;
		completedRound = false;
		completedSet = false;
	}

	
	/* ---------------------------------------------------------------------
	 * --- Getters
	 * ---------------------------------------------------------------------
	 */ 
	
	public List<HandCard> getHand() {
		return Collections.unmodifiableList(hand);
	}
	public List<Card> getStack() {
		return Collections.unmodifiableList(stack);
	}
	
	public int getID() {
		return id;
	}

	public int getSetsWon() {
		return setsWon;
	}
	
	public int getSum() {
		int sum = 0;
		for (Card c : stack) { sum += c.getValue(); }
		return sum;
	}
	
	public int getStackSize() {
		return stack.size();
	}
	
	public boolean hasPlayedTiebreaker() {
		return playedTiebreaker;
	}
	
	public boolean hasCompletedRound() {
		return completedRound;
	}
	public boolean hasCompletedSet() {
		return completedSet;
	}
	
	public String toString() {
		return playerName;
	}
	
	public boolean isControlledByAI() {
		return controlledByAI;
	}
	
	/* ---------------------------------------------------------------------
	 * --- AI controlled
	 * ---------------------------------------------------------------------
	 */ 
	
	void setControlledByAI() {
		controlledByAI = true;
	}

	/* ---------------------------------------------------------------------
	 * --- helper method
	 * ---------------------------------------------------------------------
	 */ 
	
	public boolean canPerformAction() {
		return (parentGame.getPlayer(parentGame.getPlayerTurn()).equals(this))
				 && !(completedSet || completedRound)
				 && (getStackSize() < PazaakGame.CARD_LIMIT);
	}
	
	/* ---------------------------------------------------------------------
	 * --- Outward player control methods
	 * ---------------------------------------------------------------------
	 */ 
	
	public int deal() { // returns added card's value
		if (completedDeal || !parentGame.getPlayer(parentGame.getPlayerTurn()).equals(this)) { return 0; }
		Card c = new DealerCard(); // dealer
		stack.add(c);
		completedDeal = true;
		
		completedRound = false;
		playedTiebreaker = false; // only works as last card played.

		parentGame.onPlayerCardDealt();
		if (getStackSize() == PazaakGame.CARD_LIMIT) { stand(); } // won set
		return c.getValue();
	}
	
	public int useCard(int useCard) { // returns added card's value
		if (!completedDeal) { return 0; }
		if (!canPerformAction()) { return 0; }
		
		if (useCard >= 0 && useCard <= 3) { // hand, card.
			HandCard card = hand.get(useCard);
			if (card.isUsed()) { return 0; } // can't use that card

			if (card.getEnumType() == CardNames.TIEBREAKER) {
				playedTiebreaker = true;
			}

			int val = card.getValue();
			if (card.hasStackImplications()) { // Card modifies your stack. Perform modification
				switch(card.getEnumType()) {
				case TWO_AND_FOUR:
					for (Card c : stack) { // convert to card
						if (c.getValue() == 2 || c.getValue() == 4) {
							c.flipValue();
						}
					}
					break;
					
				case THREE_AND_SIX:
					for (Card c : stack) { // convert to card
						if (c.getValue() == 3 || c.getValue() == 6) {
							c.flipValue();
						}
					}
					break;
					
				case DOUBLE:
					stack.get(stack.size()-1).doubleValue();
					break;
					
				default:
					break;
				}
			}
			stack.add(card);
			card.setUsed();
			completedRound = true;
			parentGame.onPlayerUsedCard(this);
			if (getStackSize() == PazaakGame.CARD_LIMIT) {
				stand(); // won anyways.
			}
			return val;
		}
		return 0;
	}
	
	public void stand() {
		if (!parentGame.getPlayer(parentGame.getPlayerTurn()).equals(this)) { return; }
		if (!completedDeal) { return; } // can't do shit if the deal hasn't happened
		
		completedDeal = false;
		completedSet = true;
		completedRound = true;
		parentGame.onPlayerStand(this);
	}
	
	public void endTurn() {
		if (!parentGame.getPlayer(parentGame.getPlayerTurn()).equals(this)) { return; }
		if (!completedDeal) { return; } // can't do shit if the deal hasn't happened
		completedDeal = false;
		completedRound = true;
		parentGame.onPlayerEndTurn(this);
	}	
	
	
	/* ---------------------------------------------------------------------
	 * --- Generate new set (package-only)
	 * ---------------------------------------------------------------------
	 */ 
	
	void newSet() {
		stack.clear(); // clear stack
		
		// reset state vars
		playedTiebreaker = false;
		completedDeal = false;
		completedRound = false;
		completedSet = false;
	}
	
	/* ---------------------------------------------------------------------
	 * --- Award set victory
	 * ---------------------------------------------------------------------
	 */
	
	void awardSetVictory() {
		setsWon += 1; // set won counter
	}
}
