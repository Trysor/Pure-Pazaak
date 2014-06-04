package pazaak.game;


public interface PazaakObserver {

	
	/* ---------------------------------------------------------------------
	 * --- Pazaak Observer
	 * ---------------------------------------------------------------------
	 */
	public void onSetWon(Player winner);
	public void onSetDraw();
	public void onMatchWon(Player winner);
	
	public void onPlayerTurnChanged(int playerTurn);
	public void onPlayerCardDealt(int playerTurn);
	public void onPlayerCardUsed(Player player);
}
