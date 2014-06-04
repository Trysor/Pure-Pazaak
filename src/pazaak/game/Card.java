package pazaak.game;

public abstract class Card {

	/* ---------------------------------------------------------------------
	 * --- Card Globals
	 * ---------------------------------------------------------------------
	 */ 
	static final public int DEALERMAX = 10;
	static final public int DEALERMIN = 1;
	
	static final public int HANDMAX = 6;
	static final public int HANDMIN = 1;
	
	static final public int FLIPPABLEMAX = 6;
	static final public int FLIPPABLEMIN = 1;


	/* ---------------------------------------------------------------------
	 * --- Vars
	 * ---------------------------------------------------------------------
	 */ 
	
	protected int value;
	protected CardNames enumType;
	protected boolean isSpecial;
	
	public Card(CardNames enumType) {
		this.enumType = enumType;
	}

	/* ---------------------------------------------------------------------
	 * --- Getters
	 * ---------------------------------------------------------------------
	 */ 
	public int getValue() {
		return value;
	}
	public boolean isSpecial() {
		return isSpecial;
	}
	public CardNames getEnumType() {
		return this.enumType;
	}
	
	
	/* ---------------------------------------------------------------------
	 * --- Methods
	 * ---------------------------------------------------------------------
	 */ 

	abstract void flipValue(); // differs between Dealer and Hand cards
	

	void doubleValue() {
		this.value *= 2;
	}

	public String toString() {
		return "[" + this.enumType.toString() + " (" + this.value + ")]"; // concat concat concaaaaat
	}

	
}
