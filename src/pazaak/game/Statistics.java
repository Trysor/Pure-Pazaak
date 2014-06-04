package pazaak.game;

import android.content.SharedPreferences;

public class Statistics implements PazaakObserver {
	
	
	private PazaakGame game;
	private boolean sessionHasAI;
	
	private SharedPreferences savedVars;
	private SharedPreferences.Editor savedVarsEditor;
	
	/* ---------------------------------------------------------------------
	 * --- Constructor
	 * ---------------------------------------------------------------------
	 */ 
	
	public Statistics(SharedPreferences savedVars) {	
		this.savedVars = savedVars;
		this.savedVarsEditor = savedVars.edit();
	}
	
	
	/* ---------------------------------------------------------------------
	 * --- Set new game
	 * ---------------------------------------------------------------------
	 */
	
	public void startGame(PazaakGame game) {
		this.game = game;
		sessionHasAI = game.getPlayer(1).isControlledByAI();
		this.game.addObserver(this);
		
		if (sessionHasAI) {
			savedVarsEditor.putInt("stats_num_1vAI", savedVars.getInt("stats_num_1vAI", 0)+1).commit();
		} else {
			savedVarsEditor.putInt("stats_num_1v1", savedVars.getInt("stats_num_1v1", 0)+1).commit();
		}
	}
	
	/* ---------------------------------------------------------------------
	 * --- get stat
	 * ---------------------------------------------------------------------
	 */
	
	public int getStat(String var) {
		return savedVars.getInt(var, 0);
	}
	

	
	/* ---------------------------------------------------------------------
	 * --- Register Forfeit (app ends early etc)
	 * ---------------------------------------------------------------------
	 */
	
	public void registerForfeit() {
		if (game != null && game.isGameOngoing() && sessionHasAI) {
			savedVarsEditor.putInt("stats_num_forfeits", savedVars.getInt("stats_num_forfeits", 0)+1).commit();
		}
	}



	@Override
	public void onSetWon(Player winner) {
		if (sessionHasAI) {
			if (winner.isControlledByAI()) {
				savedVarsEditor.putInt("stats_num_sets_lost", savedVars.getInt("stats_num_sets_lost", 0)+1).commit();
			} else {
				savedVarsEditor.putInt("stats_num_sets_won", savedVars.getInt("stats_num_sets_won", 0)+1).commit();
			}
		}
	}

	@Override
	public void onSetDraw() {
		if (sessionHasAI) {
			savedVarsEditor.putInt("stats_num_sets_draw", savedVars.getInt("stats_num_sets_draw", 0)+1).commit();
		}
	}

	@Override
	public void onMatchWon(Player winner) {
		if (sessionHasAI) {
			if (winner.isControlledByAI()) {
				savedVarsEditor.putInt("stats_num_matches_lost", savedVars.getInt("stats_num_matches_lost", 0)+1).commit();
			} else {
				savedVarsEditor.putInt("stats_num_matches_won", savedVars.getInt("stats_num_matches_won", 0)+1).commit();
			}
			onSetWon(winner); // the winner of the match also won the last set!
		}
	}

	@Override
	public void onPlayerTurnChanged(int playerTurn) {
		
	}

	@Override
	public void onPlayerCardUsed(Player player) {
		if (sessionHasAI && !player.isControlledByAI()) {
			savedVarsEditor.putInt("stats_num_cards_used", savedVars.getInt("stats_num_cards_used", 0)+1).commit();
		}
	}

	@Override
	public void onPlayerCardDealt(int playerTurn) {
		// TODO Auto-generated method stub
		
	}
}
