package pazaak.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PazaakGame {

	
	
	/* ---------------------------------------------------------------------
	 * --- Game Rules
	 * 
	 * To win a match, 3 sets must be won
	 * 		a match can have unlimited number of sets played (set draws are possible)
	 * 		a match is played until either player has won 3 sets.
	 * 
	 * Closest person to a sum of 20 wins a set
	 * 		exception: Tiebreaker card played wins set if otherwise a draw
	 * 		exception: 9 cards played out on the field wins a set regardless of tiebreaker and sum.
	 * 		exception: sum above 20 loses set
	 * 		exception: equal sums results in a draw. set replayed.
	 * ---------------------------------------------------------------------
	 */
	
	
	/* ---------------------------------------------------------------------
	 * --- Globals
	 * ---------------------------------------------------------------------
	 */ 

	public static final int MAX_PLAYERS = 2; // not really used internally.
	
	public static final int SUM_LIMIT = 20; // but these two are!
	public static final int CARD_LIMIT = 9; // NB!
	
	
	
	/* ---------------------------------------------------------------------
	 * --- Vars
	 * ---------------------------------------------------------------------
	 */ 
	
	private int playerTurn; // whose turn it is
	private int playerTurnInitiatedLastSet;
	private boolean gameOngoing; // basically true until someone wins
	
	private final List<Player> players;
	
	private final List<PazaakObserver> observers;
	
	private final Random randomGen;
	
	
	/* ---------------------------------------------------------------------
	 * --- Constructor
	 * ---------------------------------------------------------------------
	 */ 
	
	public PazaakGame(boolean AI) {
		randomGen = new Random();
		randomGen.setSeed(System.currentTimeMillis()); // 'cause someone said this was a good idea.
		
		this.playerTurn = randomGen.nextInt(2); // index of player starting.
		this.playerTurnInitiatedLastSet = this.playerTurn;
		this.gameOngoing = true;
		
		this.observers = new ArrayList<PazaakObserver>();
		
		this.players = new ArrayList<Player>();
		
		// Possible to fill in personalised names at some point.
		if (AI) {
			this.players.add(new Player(this, players.size(), "Player"));
			this.players.add(new Player(this, players.size(), "Computer"));
			new AI(this, getPlayer(1));
		} else {
			this.players.add(new Player(this, players.size(), "Player 1"));
			this.players.add(new Player(this, players.size(), "Player 2"));
		}
	}
	
	
	/* ---------------------------------------------------------------------
	 * --- Getters
	 * ---------------------------------------------------------------------
	 */ 

	public Player getPlayer(int index) {
		return players.get(index);
	}
	
	public int getPlayerTurn() {
		return playerTurn;
	}
	
	public boolean isGameOngoing() {
		return gameOngoing;
	}

	/* ---------------------------------------------------------------------
	 * --- Observers
	 * ---------------------------------------------------------------------
	 */

	public void addObserver(PazaakObserver obs) {
		if (!observers.contains(obs)) {
			observers.add(obs);
		}
	}
	
	public void removeObserver(PazaakObserver obs) {
		if (observers.contains(obs)) {
			observers.remove(obs);
		}
	}
	
	// -------
	
	
	private void notifyOnPlayerTurnChanged() {
		for (int i = 0; i < observers.size(); i++) { // ordered (want AI etc to get update before GUI)
			observers.get(i).onPlayerTurnChanged(playerTurn); // notify observers
		}
	}
	
	private void notifyOnPlayerCardDealt() {
		for (int i = 0; i < observers.size(); i++) { // ordered (want AI etc to get update before GUI)
			observers.get(i).onPlayerCardDealt(playerTurn);  // notify observers
		}
	}
	
	private void notifyOnPlayerCardUsed(Player player) {
		for (int i = 0; i < observers.size(); i++) { // ordered (want AI etc to get update before GUI)
			observers.get(i).onPlayerCardUsed(player); // notify observers
		}
	}
	
	private void notifyOnSetWon(Player winner) {
		for (int i = 0; i < observers.size(); i++) { // ordered (want AI etc to get update before GUI)
			observers.get(i).onSetWon(winner); // notify observers
		}
	}
	private void notifyOnSetDraw() {
		for (int i = 0; i < observers.size(); i++) { // ordered (want AI etc to get update before GUI)
			observers.get(i).onSetDraw(); // notify observers
		}
	}
	private void notifyOnMatchWon(Player winner) {
		for (int i = 0; i < observers.size(); i++) { // ordered (want AI etc to get update before GUI)
			observers.get(i).onMatchWon(winner);  // notify observers
		}
	}
	
	
	/* ---------------------------------------------------------------------
	 * --- Create Hand Card set for Game
	 * ---------------------------------------------------------------------
	 */ 
	
	public HandCard createHandCard() {
		// this can be made waaay prettier. Halp plz.
		
		
		// generate random type based on rarity
		double rarity = randomGen.nextDouble();
		if (rarity > 0.97) { // tiebreaker
			return new HandCard(CardNames.TIEBREAKER, 1);
		} else if (rarity > 0.90) { // flippable
			return new HandCard(
					CardNames.FLIPPABLE,
					Card.FLIPPABLEMIN + (int)(Math.random() * ((Card.FLIPPABLEMAX - Card.FLIPPABLEMIN) + 1))
			);
		} else if (rarity > 0.80) { // flipstack (2&4 / 3&6) card
			if (Math.random() > 0.5) {
				return new HandCard(CardNames.TWO_AND_FOUR, 0);
			}
			return new HandCard(CardNames.THREE_AND_SIX, 0);
		} else if (rarity > 0.67) { // Double card
			return new HandCard(CardNames.DOUBLE, 0);

			
		} else { // normal
			int value;
			if (Math.random() >= 0.3) { // more chance of a pos card than neg card
				value = Card.HANDMIN + (int)(Math.random() * ((Card.HANDMAX - Card.HANDMIN) + 1));
			} else {
				value = - (Card.HANDMIN + (int)(Math.random() * ((Card.HANDMAX - Card.HANDMIN) + 1)));
			}
			return new HandCard(CardNames.NORMAL, value);
		}
	}
	
	

	/* ---------------------------------------------------------------------
	 * --- award set victory helper method
	 * ---------------------------------------------------------------------
	 */
	
	private void awardSetVictory(Player winner) {
		if (!gameOngoing) { return; }
		
		winner.awardSetVictory();

		playerTurn = (playerTurnInitiatedLastSet == 0 ? 1 : 0); // The Next set is started by the one who didn't start the last one.
		playerTurnInitiatedLastSet = playerTurn;
		
		
		if (winner.getSetsWon() == 3) {
			gameOngoing = false;
			notifyOnMatchWon(winner);
			return;
		}
		for (Player p : players) {
			p.newSet(); // grant players new sets
		}
		notifyOnSetWon(winner);
	}
	
	/* ---------------------------------------------------------------------
	 * --- on Player events (package-only)
	 * ---------------------------------------------------------------------
	 */
	
	void onPlayerCardDealt() {
		notifyOnPlayerCardDealt();
	}
	
	
	void onPlayerStand(Player player) {
		if (!gameOngoing) { return; }
		
		// if the player has CARD_LIMIT cards out, its an auto-win regardless.
		if (player.getStackSize() == CARD_LIMIT) {
			awardSetVictory(player);
			return;
		}
		
		/*
		 * 		1. check for other person too; if both completed set, check scores
		 * 		2. if score is MORE than 20, give victory to other player
		 * 		3. if only this player has completed set, set playerTurn to the other player
		 */
		
		
		// check how many has completed their set now.
		int numPlayersCompetedSet = 0;
		for (Player p : players) {
			if (p.hasCompletedSet()) {
				numPlayersCompetedSet += 1;
			}
		}
		
		// who is the other guy?
		Player otherPlayer;
		if (player.getID() == 1) {
			otherPlayer = getPlayer(0);
		} else {
			otherPlayer = getPlayer(1);
		}
		
		
		// if BOTH are done with their sets
		if (numPlayersCompetedSet == 2) {
			// compare their sums
			int[] sums = { getPlayer(0).getSum(), getPlayer(1).getSum() };
			
			// check for sums above SUM_LIMIT
			if (sums[player.getID()] > SUM_LIMIT) {
				awardSetVictory(otherPlayer);
				return;
			} else if (sums[otherPlayer.getID()] > SUM_LIMIT) {
				awardSetVictory(player);
				return;
			}			
			// If a draw
			if (sums[0] == sums[1]) {				
				// check for tiebreaker cards
				int numTiebreakersPlayed = 0;
				for (Player p : players) {
					if (p.hasPlayedTiebreaker()) { numTiebreakersPlayed += 1; }
				}
				// if EXACTLY one tiebreaker card has been played, it is no longer a draw.
				if (numTiebreakersPlayed == 1) {
					// Give new sets to players and award the one with tiebreaker a set victory.
					Player winner;
					if (player.hasPlayedTiebreaker()) { // find winner
						winner = player;
					} else { winner = otherPlayer; }
					awardSetVictory(winner);
					return;
				}
				
				// oh well, its a draw.
				for (Player p : players) {
					p.newSet(); // grant players new sets
				}
				playerTurn = (playerTurnInitiatedLastSet == 0 ? 1 : 0); // The Next set is started by the one who didn't start the last one.
				playerTurnInitiatedLastSet = playerTurn;
				notifyOnSetDraw();
			} else {
				// not a draw; check the highest sum
				if (sums[player.getID()] > sums[otherPlayer.getID()]) {
					awardSetVictory(player);
					return;
				} else  {
					awardSetVictory(otherPlayer);
					return;
				}
			}
		} else {
			// only this player has completed his set. Check his sum
			if (player.getSum() > SUM_LIMIT) {
				// this player has lost the set (otherPlayer won).
				awardSetVictory(otherPlayer);
				return;
			}
			// Only one has completed his set; its the other player's turn now, then.
			playerTurn = otherPlayer.getID();
			notifyOnPlayerTurnChanged();
		}
	}

	void onPlayerEndTurn(Player player) {
		if (!gameOngoing) { return; }
		
		/*
		 * 		1. if score is MORE than SUM_LIMIT, give victory to other player
		 * 		2. give turn to other player
		 */
		
		// who is the other guy?
		Player otherPlayer = getPlayer(player.getID() == 1 ? 0 : 1);
		
		// check this player's sum
		if (player.getSum() > SUM_LIMIT) {
			// this player has lost the set.
			awardSetVictory(otherPlayer); // award otherPlayer with a set victory.
			return;
		}
	
		// The other player's turn. or is it? check.
		if (otherPlayer.hasCompletedSet()) {
			playerTurn = player.getID(); // still player's turn.
		} else { 
			playerTurn = otherPlayer.getID(); // yes it actually is the other player's turn now.
		}
		notifyOnPlayerTurnChanged();
	}
	
	void onPlayerUsedCard(Player player) {
		notifyOnPlayerCardUsed(player);
	}
}
