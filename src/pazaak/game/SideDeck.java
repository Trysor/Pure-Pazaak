package pazaak.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SideDeck {
	/*
	 * Purpose of class:
	 *  - Store side deck setup
	 *  - Load side deck setup from saved vars
	 *  - Handle 
	 * 
	 */	
	
	/* ---------------------------------------------------------------------
	 * --- Globals
	 * ---------------------------------------------------------------------
	 */ 
	
	
	// Which cards are available to an AI
	public static final CardNames[][] AI_DIFFICULTY_CARDS = {
		{
			CardNames.NORMAL,
			CardNames.DOUBLE
		},
		{
			CardNames.NORMAL,
			CardNames.DOUBLE,
			CardNames.THREE_AND_SIX, CardNames.TWO_AND_FOUR,
		},
		{
			CardNames.NORMAL,
			CardNames.DOUBLE,
			CardNames.THREE_AND_SIX, CardNames.TWO_AND_FOUR,
			CardNames.FLIPPABLE, CardNames.TIEBREAKER,
		},
	};
	

	/* ---------------------------------------------------------------------
	 * --- Locals
	 * ---------------------------------------------------------------------
	 */ 
	
	private List<CardNames> sidedeck;
	
	
	/* ---------------------------------------------------------------------
	 * --- Constructor
	 * ---------------------------------------------------------------------
	 */ 
	
	
	public SideDeck() {
		sidedeck = new ArrayList<CardNames>();
	}

	
	
	
	/* ---------------------------------------------------------------------
	 * --- save, get and load
	 * ---------------------------------------------------------------------
	 */ 
	
	/**
	 * Saves the sidedeck. Returns true if it could be saved, false otherwise.
	 *  
	 * @param sidedeck
	 * @return boolean
	 */
	public boolean saveSideDeck(CardNames[] sidedeck) {
		List<CardNames> olddeck = this.sidedeck;
		this.sidedeck.clear();
		for (CardNames card : sidedeck) {
			if (true) { // check if player has unlocked this card
				this.sidedeck.add(card);
				
			} else {
				this.sidedeck = olddeck;
				return false;
			}
		}
		
		if (this.sidedeck.size() == 30) {
			return true;
		}
		this.sidedeck = olddeck;
		return false;
	}
	
	public List<CardNames> getSideDeck() {
		return Collections.unmodifiableList(this.sidedeck);
	}

	
	
	/* ---------------------------------------------------------------------
	 * --- fdf
	 * ---------------------------------------------------------------------
	 */ 
	
	
	
	
}
