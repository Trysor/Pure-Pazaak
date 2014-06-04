package pazaak.game;

public class HandCard extends Card {

	/* ---------------------------------------------------------------------
	 * --- Vars
	 * ---------------------------------------------------------------------
	 */ 
	private boolean used;
	
	
	/* ---------------------------------------------------------------------
	 * --- Constructor
	 * ---------------------------------------------------------------------
	 */ 
	
	public HandCard(CardNames type, int val) {
		super(type);
		this.used = false;
		this.value = val;
		
		switch (type) {
			case TIEBREAKER:
			case FLIPPABLE:
			case TWO_AND_FOUR:
			case THREE_AND_SIX:
			case DOUBLE:
				this.isSpecial = true;
				return;
			case NORMAL:
				this.isSpecial = false;
				return;
			default:
				this.isSpecial = true;
				this.value = 999;
		}
	}
	

	/* ---------------------------------------------------------------------
	 * --- Getters
	 * ---------------------------------------------------------------------
	 */ 
	
	public boolean isUsed() {
		return used;
	}
	
	public boolean isFlippableCard() {
		// Implies the CARD ITSELF can be flipped before being played.
		return (super.getEnumType() == CardNames.FLIPPABLE || super.getEnumType() == CardNames.TIEBREAKER);
	}
	
	public boolean hasStackImplications() {
		return (super.getEnumType() == CardNames.TWO_AND_FOUR ||
				super.getEnumType() == CardNames.THREE_AND_SIX ||
				super.getEnumType() == CardNames.DOUBLE
				);
	}
	
	
	/* ---------------------------------------------------------------------
	 * --- Methods
	 * ---------------------------------------------------------------------
	 */ 
	
	void setUsed() {
		this.used = true;
	}
	
	void flipValue() {
		if (this.used || ((!this.used) && isFlippableCard())) {
			this.value = - this.value; // val = minus val => val flipped.
		}
	}
	
	public void flipCard() { // doesn't matter -as- much if this is abused, as if DealerCard flip was abused..
		if ((!this.used) && isFlippableCard()) {
			this.value = - this.value; // val = minus val => val flipped.
		}
	}
	
	

}
