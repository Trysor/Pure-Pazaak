package pazaak.game;

public class DealerCard extends Card {

	/* ---------------------------------------------------------------------
	 * --- Constructors
	 * ---------------------------------------------------------------------
	 */
	DealerCard(int val) {
		super(CardNames.DEALER);
		if (val >= DEALERMIN && val <= DEALERMAX) {
			this.value = val;
		} else {
			this.value = 999;
		}
		this.isSpecial = false;
	}
	
	DealerCard() {
		// is dealer card; value from 1 to 10 (double from [0, 1) to our int [1, 10])
		this(DEALERMIN + (int)(Math.random() * ((DEALERMAX - DEALERMIN) + 1)));
	}
	
	
	/* ---------------------------------------------------------------------
	 * --- Method flipValue
	 * ---------------------------------------------------------------------
	 */ 
	
	void flipValue() { // we don't ever want this public :>
		this.value = - this.value; // val = minus val => val flipped.
	}
}
