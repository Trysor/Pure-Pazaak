package pazaak.game;


import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.os.Handler;


public class AI implements PazaakObserver {

	/* ---------------------------------------------------------------------
	 * --- Artificial Intelligence
	 * 
	 * Listens to updates, and controls the assigned player
	 * Creates fictional "thinking time" through timer
	 * 
	 * Note:	Timer should be handled differently.
	 * 			Currently working for Android
	 * ---------------------------------------------------------------------
	 */ 
	
	
	/* ---------------------------------------------------------------------
	 * --- Globals
	 * ---------------------------------------------------------------------
	 */ 
	
	public static final int MAX_THINKING_TIME = 1250; // in ms
	public static final int MIN_THINKING_TIME = 750; // in ms
	
	
	public static final int LOW_END_MARGIN = PazaakGame.SUM_LIMIT - 2; // safe zone
	public static final int LOW_END_DEAL_MARGIN = PazaakGame.SUM_LIMIT - 6; // safe zone
	
	/* ---------------------------------------------------------------------
	 * --- Vars
	 * ---------------------------------------------------------------------
	 */ 
	
	private final PazaakGame parentGame;
	private final Player player;
	private final Player otherPlayer;
	
	private final List<HandCard> handAI; // Duplicate the player's information
	private final List<Card> stackAI; // Duplicate the player's information
	
	private boolean hasUsedCardThisRound;
	
	
	private final Handler handler;
	private final Timer timer;
	private final Runnable timerDoAction;
	
	
	/* ---------------------------------------------------------------------
	 * --- Constructor
	 * ---------------------------------------------------------------------
	 */ 
	
	AI(PazaakGame parentGame, Player player) {
		this.parentGame = parentGame;
		this.parentGame.addObserver(this);
		this.player = player;
		this.player.setControlledByAI();

		if (player.getID() == 1) {
			this.otherPlayer = this.parentGame.getPlayer(0);
		} else {
			this.otherPlayer = this.parentGame.getPlayer(1);
		}
		
		handAI = new ArrayList<HandCard>();
		stackAI = new ArrayList<Card>();
		
		fetchPlayerData();
		
		
		this.handler = new Handler();
		this.timer = new Timer();
		this.timerDoAction = new Runnable() {
			@Override
			public void run() {
				doBestMove();
			}
		};
	}
	
	
	/* ---------------------------------------------------------------------
	 * --- Getters
	 * ---------------------------------------------------------------------
	 */ 
	
	public PazaakGame getParentGame() {
		return parentGame;
	}
	
	public Player getPlayer() {
		return player;
	}	
	
	
	
	/* ---------------------------------------------------------------------
	 * --- Fetch Player Data
	 * ---------------------------------------------------------------------
	 */ 
	
	private void fetchPlayerData() {		
		// No need to take unnecessary risks in our stack getting faulty;
		stackAI.clear();
		for (Card c : player.getStack()) { // add the AI player's stack back in.
			if (c instanceof HandCard) {
				stackAI.add(new HandCard(c.getEnumType(), c.getValue()));
			} else {
				stackAI.add(new DealerCard(c.getValue()));
			}
		}
		// same with Hand;
		handAI.clear();
		for (Card c : player.getHand()) { // add the AI player's hand back in.
			handAI.add(new HandCard(c.getEnumType(), c.getValue()));
		}
	}
	
	
	
	/* ---------------------------------------------------------------------
	 * --- Counting Cards (feel the love, Las Vegas?)
	 * ---------------------------------------------------------------------
	 */ 
	
	private int sumAfterHandCardUsed(int handCardIndex, boolean flip) {
		if (handCardIndex >= 0 && handCardIndex <= 3) { // hand, card.
			HandCard card = handAI.get(handCardIndex);
			if (card.isUsed()) { return 0; } // can't use that card


			if (card.hasStackImplications()) { // Card modifies your stack. Perform modification
				switch(card.getEnumType()) {
				case TWO_AND_FOUR:
					for (Card c : stackAI) { // convert to card
						if (c.getValue() == 2 || c.getValue() == 4) {
							c.flipValue();
						}
					}
					break;
					
				case THREE_AND_SIX:
					for (Card c : stackAI) { // convert to card
						if (c.getValue() == 3 || c.getValue() == 6) {
							c.flipValue();
						}
					}
					break;
					
				case DOUBLE:
					stackAI.get(stackAI.size()-1).doubleValue();
					break;
					
				default:
					break;
				}
			}
			
			if (flip && card.isFlippableCard()) {
				card.flipValue();
			}
			
			stackAI.add(card);
		}
		int sum = 0; // get the change information
		for (Card c : stackAI) {
			sum += c.getValue();
		}
		fetchPlayerData(); // reset the "change"
		return sum;
	}
	
	
	
	
	/* ---------------------------------------------------------------------
	 * --- do Best Move Helper: use Card
	 * ---------------------------------------------------------------------
	 */ 
	
	private void useCard(int cardIndex, boolean flipped) {
		// System.out.println("useCard(" + cardIndex + ", " + flipped + ")");
		
		if (player.getHand().get(cardIndex).isUsed()) {
			// System.out.println("AI: ATTEMPTING TO RE-USE A CARD THAT HAS BEEN USED!");
			startAction(); // bad
			return;
		}
		
		if (flipped) {
			player.getHand().get(cardIndex).flipCard(); // oh, needs to be flipped first.
		}
		player.useCard(cardIndex);
		hasUsedCardThisRound = true;
		startAction(); // basically "stand", just delayed if timer is on.
	}
	
	
	
	
	/* ---------------------------------------------------------------------
	 * --- do Best Move
	 * ---------------------------------------------------------------------
	 */ 
	

	
	/** 
	 * Goes through (hopefully) all possible situations the AI can be in.
	 * 
	 * Step 1: Checks if it has used a card already.
	 * Step 2: Checks for direct win / lose situations, stands / ends as sees fit.
	 * Step 3: Scans Hand Cards and check for usable cards. If no usable card, stands / ends as sees fit.
	 * Step 4: Checks for necessity of using a card to not lose
	 * Step 5: Can we outright win if we use a card?
	 * Step 6: We can't win by using a card; should we use a card at all?
	 * Step 7: Basically nothing interesting to do; EndTurn.
	 * 
	 * NOTE: Does NOT take AI's Tiebreaker card into consideration. If it is played, it was purely
	 * 		 for the card value, not by the Tiebreaker effect.
	 */
	
	private void doBestMove() {
		// System.out.println("doBestMove()");
		
		
		int sum = player.getSum();
		int enemySum = otherPlayer.getSum();
		
		// STEP 1
		// System.out.println("Step 1");
		
		
		// if we've used a card, then we've got two options; Stand or endTurn. Do either of those.
		if (hasUsedCardThisRound) {
			// we've already used a card this round. Better to stand, or to end turn?
			if ((otherPlayer.getSum() > player.getSum()) && (otherPlayer.getSum() <= PazaakGame.SUM_LIMIT)) {
				player.endTurn(); // standing here means losing. It is unlikely that this situation happen.
			} else {
				if (sum >= enemySum && sum > LOW_END_DEAL_MARGIN) {
					player.stand();
					return;
				}
				player.endTurn();
			}
			return;
		}
		
		
		// STEP 2
		// System.out.println("Step 2");
		
		
		// okay, so we -can- use a card. Lets see what option proves to be the most logical
		
		// Before we scan our hand cards, though, lets make some quick checks if we can win (or lose)
		// right away, to save time and effort.
		if (sum == PazaakGame.SUM_LIMIT) {
			player.stand(); // we're spot on. Stand.
			return;
		}
		if (otherPlayer.hasCompletedSet() && enemySum < sum && sum < PazaakGame.SUM_LIMIT) {
			player.stand(); // Just stand (win) if we're ahead and the other guy has completed set already.
			return;
		}
		if (otherPlayer.hasPlayedTiebreaker() && enemySum == PazaakGame.SUM_LIMIT) {
			player.stand(); // Just feck it if otherPlayer has SUM_LIMIT and played tiebreaker
			return;
		}
		
		
		// STEP 3
		// System.out.println("Step 3");
		
		
		// Okay, time to check our Hand Cards.
		int closest = -999;
		int closestIndex = -1;
		boolean closestIsFlip = false;
		boolean canUseACard = false;
		
		// Okay. we can use our hand to our advantage. Scan them and see what we get.
		for (int i = 0; i < 4; i++) {
			HandCard card = player.getHand().get(i);
			
			if (card.isUsed()) { continue; } // can't use it anyways, skip to next
						
			int newSum = sumAfterHandCardUsed(i, false);
			
			if ((newSum > sum) && (closest <= newSum) && (newSum <= PazaakGame.SUM_LIMIT)) {
				closest = newSum;
				closestIsFlip = false;
				canUseACard = true;
				closestIndex = i; // override index
			}
			
			if (card.isFlippableCard()) {
				int newSum2 = sumAfterHandCardUsed(i, true);
				if ((newSum2 < sum) && sum <= PazaakGame.SUM_LIMIT) { continue; } // no point going backwards
				
				if ((closest < newSum2) && (newSum2 <= PazaakGame.SUM_LIMIT)) {
					closest = newSum2;
					closestIsFlip = true;
					canUseACard = true;
					closestIndex = i;
				}
			}
		}
		
		// System.out.println("canUseACard: " + canUseACard);
		
		// scan complete. We now have a card that can get us <=20, or do we?
		if (!canUseACard) {
			if (otherPlayer.hasCompletedSet() && enemySum < sum && sum < PazaakGame.SUM_LIMIT) {
				player.stand(); // Just stand (win) if we're ahead and the other guy has completed set already.
				return;
			}
			if (sum >= LOW_END_MARGIN && sum >= enemySum) {
				player.stand();
				return;
			}
			player.endTurn();
			return;
		}
		
		
		// STEP 4
		// System.out.println("Step 4");
		
		
		if (sum > PazaakGame.SUM_LIMIT) { // we're basically FORCED to use a card to NOT lose the set
			if ((enemySum > closest) && (enemySum <= PazaakGame.SUM_LIMIT)) {
				player.stand(); // fedge. losing anyways. better not to use a card.
				return;
			}
			useCard(closestIndex, closestIsFlip); // go for it. Might be lucky.
			return;
		}
		
		
		// STEP 5
		// System.out.println("Step 5");
		
		
		// a KNOWN victory
		if (otherPlayer.hasCompletedSet()
				&& enemySum >= LOW_END_DEAL_MARGIN
				&& enemySum < closest
				&& closest <= PazaakGame.SUM_LIMIT) {
			
			useCard(closestIndex, closestIsFlip); // go for it.
			return;
		}
		
		// A gambled but favourable victory
		if (enemySum < closest
				&& closest <= PazaakGame.SUM_LIMIT
				&& sum >= LOW_END_DEAL_MARGIN
				&& closest >= LOW_END_MARGIN) {
			useCard(closestIndex, closestIsFlip); // go for it.
			return;
		}
		
		// can we -draw- the set? Nevermind doing it; thats for step 6
		boolean canDraw = false;
		if (otherPlayer.hasCompletedSet()
				&& enemySum == closest // .. a draw?
				&& !otherPlayer.hasPlayedTiebreaker() // yes, a draw.
				&& closest <= PazaakGame.SUM_LIMIT // no point if the draw is basically losing.
				&& enemySum >= LOW_END_MARGIN) { // is it even reasonable to draw?
			canDraw = true; // a KNOWN draw
		}
		
		
		// STEP 6
		// System.out.println("Step 6");
		
		
		// Should we use a card at all? We're not in a situation where we are going to win by doing so.
		
		// high sum
		if (sum >= LOW_END_MARGIN) {
			// NOT a comfortable place to be when we can't win this turn!
			if (canDraw) {
				useCard(closestIndex, closestIsFlip); // we better draw
			} else {
				// and we can't even do a reasonable draw.. 
				if (enemySum > sum) {
					player.endTurn(); // Zoinks! Crazy CRAZY gamble time
				} else {
					player.stand();
				}
			}
			return;
		}
		// medium sum
		if (sum >= LOW_END_DEAL_MARGIN && sum < LOW_END_MARGIN) {
			if (canDraw) {
				useCard(closestIndex, closestIsFlip); // we better draw
				return;
			}
		}

		
		// STEP 7
		// System.out.println("Step 7");
		
		
		// Basically nothing interesting to do.
		player.endTurn();
	}
	
	
	
	
	/* ---------------------------------------------------------------------
	 * --- Event helper
	 * ---------------------------------------------------------------------
	 */
	
	private void startTurn() {
		fetchPlayerData(); // querying fresh data EVERY time to avoid stack / hand mistakes in AI
	}

	private void startAction() {
		if (player.getStackSize() == PazaakGame.CARD_LIMIT) {
			return; // 9 cards filling all 9 card slots. stand() handled in player object code.
		}
		
		TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
				handler.post(timerDoAction);
			}
		};
		this.timer.schedule(timerTask, (long) (MIN_THINKING_TIME + (Math.random() * ((MAX_THINKING_TIME - MIN_THINKING_TIME) + 1)))); 
	}
	
	
	/* ---------------------------------------------------------------------
	 * --- Event Listeners
	 * ---------------------------------------------------------------------
	 */ 
	
	@Override
	public void onSetWon(Player winner) {
		hasUsedCardThisRound = false;
		if (player.getID() == parentGame.getPlayerTurn()) { // Is it our turn?
			startTurn();
			startAction();
		}
	}
	@Override
	public void onSetDraw() {
		hasUsedCardThisRound = false;
		if (player.getID() == parentGame.getPlayerTurn()) { // Is it our turn?
			startTurn();
			startAction();
		}
	}
	@Override
	public void onMatchWon(Player winner) {
	}
	@Override
	public void onPlayerTurnChanged(int playerTurn) {
		hasUsedCardThisRound = false;		
		if (player.getID() == playerTurn) { // Is it our turn?
			startTurn();
		}
	}
	@Override
	public void onPlayerCardUsed(Player player) {
		
	}


	@Override
	public void onPlayerCardDealt(int playerTurn) {
		hasUsedCardThisRound = false;		
		if (player.getID() == playerTurn) { // Is it our turn?
			startTurn();
			startAction();
		}
	}

	
}
