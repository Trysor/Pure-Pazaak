package pazaak.android;

import pazaak.game.Card;
import pazaak.game.HandCard;
import pazaak.game.PazaakGame;
import pazaak.game.PazaakObserver;
import pazaak.game.Player;
import pazaak.game.Statistics;
import pazaak.gui.BitmapHandler;
import pazaak.gui.GUIButton;
import pazaak.gui.GUICard;
import pazaak.gui.GUIImage;
import pazaak.gui.GUIText;
import pazaak.gui.GUIToggle;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;



@SuppressLint("ViewConstructor")
public class MainGamePanel extends SurfaceView implements SurfaceHolder.Callback, PazaakObserver {

	public static final int MENU_STATE_MAIN_MENU = 0;
	public static final int MENU_STATE_STORY = 1;
	public static final int MENU_STATE_INSTRUCTIONS = 2;
	public static final int MENU_STATE_CREDITS = 3;
	public static final int MENU_STATE_STATISTICS = 4;
	
	public static final int GAME_PLAYING_STATE_MENU = 0;
	public static final int GAME_PLAYING_STATE_PLAYING = 1;
	public static final int GAME_PLAYING_STATE_WON = 2;
	
	private static final String TAG = MainGamePanel.class.getSimpleName();
	
	private GameSound sound;
	private Statistics stats;

	// GUI LEVEL 1
	private GUIImage background;
	
	// GUI LEVEL 2
	private GUIImage mainMenu;
	private GUIImage mainMenuInfo;
	
	private GUIButton[] gamefieldButtons;
	private GUICard[][] gamefieldCards;
	private GUICard[][] handCards;
	private GUICard draggedCard;
	private GUIText[] playerNames;
	private GUIText[] playerScores;
	private GUIImage[][] playerSetIcon;
	
	// GUI LEVEL 3
	private GUIText[] statisticsTexts;
	private GUIButton[] menuButtons;
	
	private GUIImage notificationBox;
	private GUIText notificationText;
	private GUIButton notificationButton;
	
	private GUIToggle[] soundToggles;
	
	
	
	// BitmapHandler
	private BitmapHandler bitmapHandler;

	private MainThread thread;
	private boolean loaded;
	private PazaakGame game;
	
	private int gamePlayingState;
	private int menuState;
	
	public final Typeface font = Typeface.createFromAsset(getResources().getAssets(), "fonts/zeroes.TTF");
	

	
	// canvas scale
	private double scale;
	private double height;
	
	// FPS counter
	private GUIText FPSCounter;
	private long lastFPSUpdate;
	private int[] FPSArray;
	private int FPSTickSum;
	private int FPSTickIndex;


    
    
	/* ---------------------------------------------------------------------
	 * --- Constructor
	 * ---------------------------------------------------------------------
	 */ 
	
	

	public MainGamePanel(Context context, GameSound sound, Statistics stats) {
		super(context);
		// adding the callback (this) to the surface holder to intercept events
		getHolder().addCallback(this);
		this.sound = sound;
		this.stats = stats;
		
		// Bitmap Handler
		this.scale = (double)getResources().getDisplayMetrics().widthPixels/(double)1280;
		this.height = (double)(scale*(double)720);
		bitmapHandler = new BitmapHandler(this);
		
		
		// States
		gamePlayingState = GAME_PLAYING_STATE_MENU; // not playing.
		menuState = MENU_STATE_MAIN_MENU; // main menu
		
		// Create Arrays
		gamefieldButtons = new GUIButton[2];
		gamefieldCards = new GUICard[2][9];
		handCards = new GUICard[2][4];
		playerNames = new GUIText[2];
		playerScores = new GUIText[2];
		playerSetIcon = new GUIImage[2][9];
		statisticsTexts = new GUIText[10];
		menuButtons = new GUIButton[7];
		soundToggles = new GUIToggle[2];
		
		
		
		// FPS related stuff
		lastFPSUpdate = System.nanoTime();
		FPSTickSum = 0;
	    FPSTickIndex = 0;
		FPSArray = new int[100];
		for (int i = 0; i < 100; i++) {
			FPSArray[i] = 0;
		}
		
		
		// create the game loop thread
		this.thread = new MainThread(getHolder(), this);
		loaded = false;
		
		// make the GamePanel focusable so it can handle events
		setFocusable(true);
	}
	
	
	public double getScale() {
		return scale;
	}
	public double getGameHeight() {
		return height;
	}
	
	public int getGamePlayingState() {
		return gamePlayingState;
	}
	
	public BitmapHandler getBitmapHandler() {
		return bitmapHandler;
	}
	

	/* ---------------------------------------------------------------------
	 * --- Game related methods
	 * ---------------------------------------------------------------------
	 */ 	
	
	private void startGame(boolean AI) {
		game = new PazaakGame(AI); // game with AI.
		game.addObserver(this);
		stats.startGame(game);
		gamePlayingState = GAME_PLAYING_STATE_PLAYING; // playing
		
		// Player Names		
		playerNames[0].setText(game.getPlayer(0).toString());
		playerNames[1].setText(game.getPlayer(1).toString());
		
		// player hands
		for (int i = 0; i < handCards[0].length; i++) {
			GUICard c =handCards[0][i];
			c.setCard(game.getPlayer(0).getHand().get(c.getCardNumber()));
		}
		for (int i = 0; i < handCards[1].length; i++) {
			GUICard c = handCards[1][i];
			c.setCard(game.getPlayer(1).getHand().get(c.getCardNumber()));
		}
		
		game.getPlayer(game.getPlayerTurn()).deal(); // add initial dealer card for the first player
		getGameInformation(); // update our GUI
	}
	
	public PazaakGame getGame() {
		return game;
	}
	
	private void closeGame() {
		for (int i = 0; i < gamefieldCards[0].length; i++) {
			gamefieldCards[0][i].setCard(null);
		}
		for (int i = 0; i < gamefieldCards[1].length; i++) {
			gamefieldCards[1][i].setCard(null);
		}
		
		for (int i = 0; i < handCards[0].length; i++) {
			GUICard c = handCards[0][i];
			c.resetPos();
			c.setCard(null);
		}
		for (int i = 0; i < handCards[1].length; i++) {
			GUICard c = handCards[1][i];
			c.resetPos();
			c.setCard(null);
		}
		
		gamePlayingState = GAME_PLAYING_STATE_MENU;
		menuState = MENU_STATE_MAIN_MENU;
		game = null;
	}
	
	public void loadGUI() {
		if (loaded) { return; }

		String[] menuButtonTexts = getResources().getStringArray(R.array.menu_button_texts);
		String[] gamefieldButtonTexts = getResources().getStringArray(R.array.gamefield_button_texts);
		
		
		// GUI LEVEL 1
		background = new GUIImage(getWidth()/2, getHeight()/2);
		background.setBitmap(bitmapHandler.getBackground());
		
		// GUI LEVEL 2
		mainMenu = new GUIImage(getWidth()/2, getHeight()/2);
		mainMenu.setBitmap(bitmapHandler.getMenu());
		
		mainMenuInfo = new GUIImage(getWidth()/2, getHeight()/2);
		
		
		int cardID = 0;
		for (int row = 1; row <= 3; row++) {
			for (int column = 1; column <= 3; column++) { // gamefieldcards player 1
				gamefieldCards[0][cardID] = new GUICard(this, false, 0, cardID,
					getWidth()/2 - (int) ((170 + (3-column)*96)*scale),
					getHeight()/2 - (int) ((184 - (row-1)*134)*scale)
				);
				cardID += 1;
			}
		}
		cardID = 0;
		for (int row = 1; row <= 3; row++) {
			for (int column = 1; column <= 3; column++) { // gamefieldcards player 2
				gamefieldCards[1][cardID] = new GUICard(this, false, 1, cardID,
					getWidth()/2 + (int) ((172 + (column-1)*96)*scale),
					getHeight()/2 - (int) ((184 - (row-1)*134)*scale)
				);
				cardID += 1;
			}
		}
		for (int column = 1; column <= 4; column++) { // handcards player 1
			handCards[0][column-1] = new GUICard(this, true, 0, column-1,
				getWidth()/2  - (int) ((121 + (column-1)*96)*scale),
				(int) ((getHeight()/2)+230*scale)
					);
		}
		for (int column = 1; column <= 4; column++) { // handcards player 2
			handCards[1][column-1] = new GUICard(this, true, 1, column-1,
				getWidth()/2  + (int) ((124 + (4-column)*96)*scale),
				(int) ((getHeight()/2)+230*scale)
			);
		}

		
		for (int i = 1; i <= 3; i++) { // Set Icons
			 // player1
			playerSetIcon[0][i] = new GUIImage(getWidth()/2 - (int) (525*scale), getHeight()/2 - (int) (((3-i)*64+25)*scale) );
			playerSetIcon[0][i].setBitmap(bitmapHandler.getSetlight());
			 // player2
			playerSetIcon[1][i] = new GUIImage(getWidth()/2 + (int) (532*scale), getHeight()/2 - (int) (((3-i)*64+25)*scale));
			playerSetIcon[1][i].setBitmap(bitmapHandler.getSetlight());
		}
		
		playerNames[0] = new GUIText(this, getWidth()/2 - (int)(268*scale), getHeight()/2 - (int)(260*scale), (int)(35*scale), Paint.Align.CENTER);
		playerNames[1] = new GUIText(this, getWidth()/2 + (int)(268*scale),	getHeight()/2 - (int)(260*scale), (int)(35*scale), Paint.Align.CENTER);
		
		playerScores[0] = new GUIText(this, getWidth()/2 - (int) (505*scale), getHeight()/2 - (int) (195*scale), (int)(55*this.scale), Paint.Align.CENTER);
		playerScores[1] = new GUIText(this, getWidth()/2 + (int) (500*scale), getHeight()/2 - (int) (195*scale), (int)(55*this.scale), Paint.Align.CENTER);

		
		gamefieldButtons[0] = new GUIButton(this, bitmapHandler.getButton(), gamefieldButtonTexts[0], getWidth()/2, (int) ((getHeight()/2)+25*scale) );
		gamefieldButtons[1] = new GUIButton(this, bitmapHandler.getButton(), gamefieldButtonTexts[1], getWidth()/2, (int) ((getHeight()/2)+100*scale)  );
		
		// GUI LEVEL 3
		
		menuButtons[0] = new GUIButton(this, bitmapHandler.getButtonlong(), menuButtonTexts[0], getWidth()/2, (int) ((getHeight()/2)-110*scale));
		menuButtons[1] = new GUIButton(this, bitmapHandler.getButtonlong(), menuButtonTexts[1], getWidth()/2, (int) ((getHeight()/2)-60*scale));
		menuButtons[2] = new GUIButton(this, bitmapHandler.getButtonlong(), menuButtonTexts[2], getWidth()/2, (int) ((getHeight()/2)+40*scale));
		menuButtons[3] = new GUIButton(this, bitmapHandler.getButtonlong(), menuButtonTexts[3], getWidth()/2, (int) ((getHeight()/2)+90*scale));
		menuButtons[4] = new GUIButton(this, bitmapHandler.getButtonlong(), menuButtonTexts[4], getWidth()/2, (int) ((getHeight()/2)+140*scale));
		menuButtons[5] = new GUIButton(this, bitmapHandler.getButtonlong(), menuButtonTexts[5], getWidth()/2, (int) ((getHeight()/2)+190*scale));
		menuButtons[6] = new GUIButton(this, bitmapHandler.getButton(), menuButtonTexts[6], getWidth()/2, (int) ((getHeight()/2)+235*scale));
		
		
		 // "stats_num_1vAI"
		statisticsTexts[0] = new GUIText(this, getWidth()/2+(int)(241*scale), getHeight()/2-(int)(85*scale), (int)(25*scale), Paint.Align.RIGHT);
		// "stats_num_1v1"
		statisticsTexts[1] = new GUIText(this, getWidth()/2+(int)(241*scale), getHeight()/2-(int)(55*scale), (int)(25*scale), Paint.Align.RIGHT);
		// "games_won_ratio"	
		statisticsTexts[2] = new GUIText(this, getWidth()/2, getHeight()/2+(int)(40*scale), (int)(45*scale), Paint.Align.CENTER);
		// "stats_num_matches_won"
		statisticsTexts[3] = new GUIText(this, getWidth()/2-(int)(20*scale), getHeight()/2+(int)(58*scale), (int)(20*scale), Paint.Align.RIGHT);
		// "stats_num_matches_lost"
		statisticsTexts[4] = new GUIText(this, getWidth()/2-(int)(20*scale), getHeight()/2+(int)(78*scale), (int)(20*scale), Paint.Align.RIGHT);
		// "stats_num_forfeits"
		statisticsTexts[5] = new GUIText(this, getWidth()/2-(int)(20*scale), getHeight()/2+(int)(98*scale),	(int)(20*scale), Paint.Align.RIGHT);
		// "stats_num_cards_used"		
		statisticsTexts[6] = new GUIText(this, getWidth()/2-(int)(20*scale), getHeight()/2+(int)(137*scale), (int)(20*scale), Paint.Align.RIGHT);
		// "stats_num_sets_won"
		statisticsTexts[7] = new GUIText(this, getWidth()/2+(int)(241*scale), getHeight()/2+(int)(58*scale), (int)(20*scale), Paint.Align.RIGHT);
		// "stats_num_sets_lost"
		statisticsTexts[8] = new GUIText(this, getWidth()/2+(int)(241*scale), getHeight()/2+(int)(78*scale), (int)(20*scale), Paint.Align.RIGHT);
		// "stats_num_sets_draw"
		statisticsTexts[9] = new GUIText(this, getWidth()/2+(int)(241*scale), getHeight()/2+(int)(98*scale), (int)(20*scale), Paint.Align.RIGHT);
		
		
		notificationBox = new GUIImage(getWidth()/2, getHeight()/2);
		notificationBox.setBitmap(bitmapHandler.getNotificationbox());
		notificationText = new GUIText(this, getWidth()/2, getHeight()/2 - (int)(75*scale), (int)(35*scale), Paint.Align.CENTER);
		notificationButton = new GUIButton(this, bitmapHandler.getButton(), gamefieldButtonTexts[2], getWidth()/2, (int)((getHeight()/2)-25*scale));


		soundToggles[0] = new GUIToggle(this, bitmapHandler.getMusicicon(), bitmapHandler.getMusicicondisabled(), sound.isMusicEnabled(),
				getWidth()/2 + bitmapHandler.getBackground().getWidth()/2 - (int)((5+25)*scale),
				getHeight()/2 - bitmapHandler.getBackground().getHeight()/2 + (int)(26*scale)
		);
		soundToggles[1] = new GUIToggle(this, bitmapHandler.getSoundicon(), bitmapHandler.getSoundicondisabled(), sound.isSoundEnabled(),
				getWidth()/2 + bitmapHandler.getBackground().getWidth()/2 - (int)((5+25*3+10)*scale),
				getHeight()/2 - bitmapHandler.getBackground().getHeight()/2 + (int)(26*scale)
		);
		
		this.FPSCounter = new GUIText(this, 
				(int)(25*scale),
				getHeight()/2 - (int)((bitmapHandler.getBackground().getHeight()/2) - 30*scale),
				(int)(22*scale), Paint.Align.LEFT
		);
	
		
		loaded = true;
	}
		



	/* ---------------------------------------------------------------------
	 * --- Surface
	 * ---------------------------------------------------------------------
	 */
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		loadGUI();
		
		try {
			thread.setRunning(true);
			thread.start();
		} catch (Exception e) {
			// thread has already been started, yet its not running, or something.
		}
	}
	
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.d(TAG, "Surface is being destroyed");
		// tell the thread to shut down and wait for it to finish
		// this is a clean shutdown
		boolean retry = true;
		while (retry) {
			try {
				thread.setRunning(false);
				thread.join();
				
				retry = false;
			} catch (InterruptedException e) {
				// try again shutting down the thread
			} catch (Exception e) {
				// FATAL?
			}
		}
		//closeGame();
		Log.d(TAG, "Thread was shut down cleanly");
	}
	
	
	/* ---------------------------------------------------------------------
	 * --- Touch events
	 * ---------------------------------------------------------------------
	 */ 
	
	
	
	public synchronized boolean onTouchEvent(MotionEvent event) {
		int action = event.getAction();
		int x = (int) event.getX();
		int y = (int) event.getY();

		
		switch(action) {

		case MotionEvent.ACTION_DOWN:
			if (gamePlayingState == GAME_PLAYING_STATE_MENU) { // We are on the menu
				if (menuState > MENU_STATE_MAIN_MENU) {
					menuButtons[menuButtons.length-1].handleActionDown(x,y); // not mainmainu, nor playing, so we need back button
				} else { // Main Menu
					for (int index = 0; index < (menuButtons.length-1); index++) { // last value is back button. Main menu doesnt have "back"
						menuButtons[index].handleActionDown(x,y);
					}
				}
				
			} else if (gamePlayingState == GAME_PLAYING_STATE_PLAYING) { // we are playing
				
				for (int playerID = 0; playerID <= 1; playerID++) {
					for (int cardID = 0; cardID <= 3; cardID++) {
						handCards[playerID][cardID].handleActionDown(x, y);
					}
				}
				if (draggedCard == null) {
					gamefieldButtons[0].handleActionDown(x,y); // stand
					gamefieldButtons[1].handleActionDown(x,y); // end turn
				}
				
			} else if (gamePlayingState == GAME_PLAYING_STATE_WON) { // someone won
				notificationButton.handleActionDown(x,y);
			}
			
			soundToggles[0].handleActionDown(x,y); // music
			soundToggles[1].handleActionDown(x,y); // sound effects
			
			break;
		
		case MotionEvent.ACTION_MOVE:
			if (gamePlayingState == GAME_PLAYING_STATE_MENU) { // We are on the menu
				if (menuState > MENU_STATE_MAIN_MENU) {
					menuButtons[menuButtons.length-1].handleActionDown(x,y); // not mainmainu, nor playing, so we need back button
				} else { // Main Menu
					for (int index = 0; index < (menuButtons.length-1); index++) { // last value is back button. Main menu doesnt have "back"
						menuButtons[index].handleActionDown(x,y);
					}
				}
			} else if (gamePlayingState == GAME_PLAYING_STATE_PLAYING) { // we are playing
				
				if (draggedCard == null) { // when we move, we do not want ANYTHING else to be touched!
					for (int playerID = 0; playerID <= 1; playerID++) {
						for (int cardID = 0; cardID <= 3; cardID++) {
							handCards[playerID][cardID].handleActionDown(x, y);
						}
					}
					gamefieldButtons[0].handleActionDown(x,y); // stand
					gamefieldButtons[1].handleActionDown(x,y); // end turn
				}
				
				// If we are dragging a card, then lets notice that.
				dragloop:
				for (int playerID = 0; playerID <= 1; playerID++) {
					for (int cardID = 0; cardID <= 3; cardID++) {
						boolean dragging = handCards[playerID][cardID].handleActionMove(x, y);
						if (dragging) {
							draggedCard = handCards[playerID][cardID];
							break dragloop;
						}
					}
				}
				
			} else if (gamePlayingState == GAME_PLAYING_STATE_WON) { // someone won
				notificationButton.handleActionDown(x,y);
			}
			
			soundToggles[0].handleActionDown(x,y); // music
			soundToggles[1].handleActionDown(x,y); // sound effects
			
			break;
			
		case MotionEvent.ACTION_UP:
			if (gamePlayingState == GAME_PLAYING_STATE_MENU) { // We are on the menu
				for (int index = 0; index < menuButtons.length; index++) { 
					menuButtons[index].setTouched(false, true);
				}
			} else if (gamePlayingState == GAME_PLAYING_STATE_PLAYING) { // we are playing
				draggedCard = null;
				gamefieldButtons[0].setTouched(false, true);	
				gamefieldButtons[1].setTouched(false, true);	

				for (int playerID = 0; playerID <= 1; playerID++) {
					for (int cardID = 0; cardID <= 3; cardID++) {
						handCards[playerID][cardID].handleActionUp(x, y);
					}
				}
			} else if (gamePlayingState == GAME_PLAYING_STATE_WON) { // someone won
				notificationButton.setTouched(false, true);
			}
			
			
			soundToggles[0].setTouched(false, true);
			soundToggles[1].setTouched(false, true);
			break;
			

		case MotionEvent.ACTION_CANCEL:
			if (gamePlayingState == GAME_PLAYING_STATE_MENU) { // We are on the menu
				for (int index = 0; index < menuButtons.length; index++) { 
					menuButtons[index].setTouched(false, false);
				}
			} else if (gamePlayingState == GAME_PLAYING_STATE_PLAYING) { // we are playing
				draggedCard = null;
				gamefieldButtons[0].setTouched(false, false);	
				gamefieldButtons[1].setTouched(false, false);	

				for (int playerID = 0; playerID <= 1; playerID++) {
					for (int cardID = 0; cardID <= 3; cardID++) {
						handCards[playerID][cardID].handleActionUp(x, y);
					}
				}
			} else if (gamePlayingState == GAME_PLAYING_STATE_WON) { // someone won
				notificationButton.setTouched(false, false);
			}
			
			soundToggles[0].setTouched(false, false);
			soundToggles[1].setTouched(false, false);
			break;
		}
		
		return true;
	}
	
	
	/* ---------------------------------------------------------------------
	 * --- Handle Clicks
	 * ---------------------------------------------------------------------
	 */ 
	
	
	public synchronized void handleClicks(GUIButton b) {
		sound.playSound("button");

		if (gamePlayingState == GAME_PLAYING_STATE_PLAYING) {
			if (b == gamefieldButtons[0]) { // stand
				if (game.getPlayer(game.getPlayerTurn()).isControlledByAI()) {
					return;
				}
				game.getPlayer(game.getPlayerTurn()).stand();
				
				
				
				
			} else if (b == gamefieldButtons[1]) { // end turn
				if (game.getPlayer(game.getPlayerTurn()).isControlledByAI()) {
					return;
				}
				game.getPlayer(game.getPlayerTurn()).endTurn();
				
				
			}	
			return;
		}
		if  ((gamePlayingState == GAME_PLAYING_STATE_WON) && b == notificationButton) { // someone won, go to menu
			closeGame();
			return;
		}		
		
		
		if (b == menuButtons[0]) { // 1 player game.
			startGame(true);

		} else if (b == menuButtons[1]) { // 2 player game.
			startGame(false); // no AI.
			
		} else if (b == menuButtons[2]) { // story
			mainMenuInfo.setBitmap(bitmapHandler.getStory());
			menuState = MENU_STATE_STORY;
			
		} else if (b == menuButtons[3]) { // instructions
			mainMenuInfo.setBitmap(bitmapHandler.getInstructions());
			menuState = MENU_STATE_INSTRUCTIONS;
			
		} else if (b == menuButtons[4]) { // credits
			mainMenuInfo.setBitmap(bitmapHandler.getCredits());
			menuState = MENU_STATE_CREDITS;
			
		} else if (b == menuButtons[5]) { // stats
			mainMenuInfo.setBitmap(bitmapHandler.getStatistics());
			
			String[] stats = getResources().getStringArray(R.array.statistics);
			statisticsTexts[0].setText(String.valueOf(this.stats.getStat(stats[0]))); // "stats_num_1vAI"
			statisticsTexts[1].setText(String.valueOf(this.stats.getStat(stats[1]))); // "stats_num_1v1"
			//statisticsTexts[2].setText(String.valueOf(this.stats.getStat(stats[2]))); // "games_won_ratio"  see below
			statisticsTexts[3].setText(String.valueOf(this.stats.getStat(stats[3]))); // "stats_num_matches_won"
			statisticsTexts[4].setText(String.valueOf(this.stats.getStat(stats[4]))); // "stats_num_matches_lost"
			statisticsTexts[5].setText(String.valueOf(this.stats.getStat(stats[5]))); // "stats_num_forfeits"
			statisticsTexts[6].setText(String.valueOf(this.stats.getStat(stats[6]))); // "stats_num_cards_used"
			statisticsTexts[7].setText(String.valueOf(this.stats.getStat(stats[7]))); // "stats_num_sets_won"
			statisticsTexts[8].setText(String.valueOf(this.stats.getStat(stats[8]))); // "stats_num_sets_lost"
			statisticsTexts[9].setText(String.valueOf(this.stats.getStat(stats[9]))); 	// "stats_num_sets_draw"
					
			if (!(this.stats.getStat("stats_num_1vAI") == 0)) {
				statisticsTexts[2].setText(String.format("%.2f", (double)
							(((double)this.stats.getStat("stats_num_matches_won")/(double)this.stats.getStat("stats_num_1vAI"))*100))+"%");
			} else { statisticsTexts[2].setText("NaN"); }
			
			menuState = MENU_STATE_STATISTICS;
			
			
		} else if (b == menuButtons[6]) {
			menuState = MENU_STATE_MAIN_MENU;
		}
	}
	
	public synchronized void handleToggle(GUIToggle t) {
		if (t == soundToggles[0]) {
			sound.enableMusic(soundToggles[0].getToggledState());
		} else if (t == soundToggles[1]) {
			sound.enableSound(soundToggles[1].getToggledState());
		}
		
	}
	
	
	public synchronized void handleCardClicks(GUICard c) {
		Card card = c.getCard();
		if (card instanceof HandCard && ((HandCard) card).isFlippableCard()) {
			((HandCard) card).flipCard();
			//c.setCard(card);
		}
		getGameInformation();
	}
	
	public synchronized void handleCardMoves(GUICard c) {
		this.game.getPlayer(game.getPlayerTurn()).useCard(c.getCardNumber());
		
		if (this.game.getPlayer(game.getPlayerTurn()).getSum() == PazaakGame.SUM_LIMIT) {
			this.game.getPlayer(game.getPlayerTurn()).stand();
		}
		getGameInformation();
	}

	
	/* ---------------------------------------------------------------------
	 * --- Render
	 * ---------------------------------------------------------------------
	 */ 
	
	
	public synchronized void render(Canvas canvas) {
		background.draw(canvas);
		
		
		if (gamePlayingState == GAME_PLAYING_STATE_MENU) { // menu
			
			mainMenu.draw(canvas); // at the menu screen
			if (menuState > MENU_STATE_MAIN_MENU) {
				mainMenuInfo.draw(canvas);
				menuButtons[menuButtons.length-1].draw(canvas); // not mainmainu, nor playing, so we need back button
				
				if (menuState == MENU_STATE_STATISTICS) { // Statistics
					for (int i = 0; i < statisticsTexts.length; i++) {
						statisticsTexts[i].draw(canvas);
					}
				}
				
				
			} else { // Main Menu
				for (int index = 0; index < (menuButtons.length-1); index++) { // last value is back button. Main menu doesnt have "back"
					menuButtons[index].draw(canvas);
				}
			}
			
			
		} else if (gamePlayingState == GAME_PLAYING_STATE_PLAYING || gamePlayingState == GAME_PLAYING_STATE_WON) { // playing or won
			
			for (int playerID = 0; playerID <= 1; playerID++) {
				// Set icons
				for (int i = 1; i <= 3; i++) {
					if (game.getPlayer(playerID).getSetsWon() >= i) {
						playerSetIcon[playerID][i].draw(canvas);
					}	
				}
				// Gamefield Cards
				for (int cardIndex = 0; cardIndex < 9; cardIndex++) {
					if (gamefieldCards[playerID][cardIndex].hasCard()) {
						gamefieldCards[playerID][cardIndex].draw(canvas);
					}
				}
				// Hand Cards
				for (int cardIndex = 0; cardIndex < 4; cardIndex++) {
					if (handCards[playerID][cardIndex].hasCard()) {
						handCards[playerID][cardIndex].draw(canvas);
					}
				}
				// Player name
				playerNames[playerID].draw(canvas);
				playerScores[playerID].draw(canvas);
			}
			
			gamefieldButtons[0].draw(canvas);
			gamefieldButtons[1].draw(canvas);
	

			if (gamePlayingState == GAME_PLAYING_STATE_WON) {
				notificationBox.draw(canvas);
				notificationText.draw(canvas);
				notificationButton.draw(canvas);
			}
		}
		
	

		soundToggles[0].draw(canvas);
		soundToggles[1].draw(canvas);
		
		long now = System.nanoTime();
		FPSCounter.setText("FPS: " + CalcFPSTick((int)((1/((now-lastFPSUpdate)/Math.pow(10,9))))));
		FPSCounter.draw(canvas);
		
		// make sure we render the dragged card over every other element
		if (draggedCard != null) {
			draggedCard.draw(canvas);
		}
		
		lastFPSUpdate = now;
	}
	
	
	/* ---------------------------------------------------------------------
	 * --- CalcFPS
	 * ---------------------------------------------------------------------
	 */ 
	
	
	public double CalcFPSTick(int newtick) {
	    FPSTickSum -= FPSArray[FPSTickIndex];  /* subtract value falling off */
	    FPSTickSum += newtick;              /* add new value */
	    FPSArray[FPSTickIndex]=newtick;   /* save new value so it can be subtracted later */
	    if (++FPSTickIndex== 100) {    /* inc buffer index */
	    	FPSTickIndex=0;
	    }
	    /* return average */
	    return ((double) FPSTickSum/100);
	}
	
	
	
	/* ---------------------------------------------------------------------
	 * --- GUI update helpers
	 * ---------------------------------------------------------------------
	 */ 
	

	private void announceWinner(Player winner) {
		if (game.isGameOngoing()) { return; }
		notificationText.setText(winner.toString().concat(" won the match!"));
		gamePlayingState = GAME_PLAYING_STATE_WON;
		

	}
	
	private void getGameInformation() {
		playerScores[0].setText(String.valueOf(game.getPlayer(0).getSum()));
		playerScores[1].setText(String.valueOf(game.getPlayer(1).getSum()));
		
		playerScores[0].setTextMode(game.getPlayer(0).getSum() > PazaakGame.SUM_LIMIT ? "red" : "normal");
		playerScores[1].setTextMode(game.getPlayer(1).getSum() > PazaakGame.SUM_LIMIT ? "red" : "normal");
		
		animNames();
		
		// update every card
		for (int playerID = 0; playerID <= 1; playerID++) {
			// Gamefield Cards
			for (int cardIndex = 0; cardIndex < 9; cardIndex++) {
				GUICard gc = gamefieldCards[playerID][cardIndex];
				if (gc.getCardNumber() < game.getPlayer(playerID).getStack().size()) {
					gc.setCard(game.getPlayer(playerID).getStack().get(gc.getCardNumber()));
				} else { gc.setCard(null); }
			}
			// Hand Cards
			for (int cardIndex = 0; cardIndex < 4; cardIndex++) {
				handCards[playerID][cardIndex].setCardGraphics();
			}
		}
	}
	
	private void animNames() {
		if (game.getPlayerTurn() == 0) { // player 1
			playerNames[0].setTextMode("animate");
			playerNames[1].setTextMode(game.getPlayer(1).hasCompletedSet() ? "stand" : "normal");
		} else { // player 2
			playerNames[0].setTextMode(game.getPlayer(0).hasCompletedSet() ? "stand" : "normal");
			playerNames[1].setTextMode("animate");
		}
	}
	
	
	
	/* ---------------------------------------------------------------------
	 * --- Event Handlers
	 * ---------------------------------------------------------------------
	 */ 

	@Override
	public void onSetWon(Player winner) {
		game.getPlayer(game.getPlayerTurn()).deal();
		getGameInformation();
		
		sound.playSound("set");
	}
	@Override
	public void onSetDraw() {
		game.getPlayer(game.getPlayerTurn()).deal();
		getGameInformation();
		
		sound.playSound("draw");
	}
	@Override
	public void onMatchWon(Player winner) {
		getGameInformation();
		announceWinner(winner); // Announce Winner!
		
		sound.playSound("match");
	}
	@Override
	public void onPlayerTurnChanged(int playerTurn) {
		game.getPlayer(playerTurn).deal();
		getGameInformation();
		
		Log.d(TAG, "onPlayerTurnChanged");

		sound.playSound("dealercard");
	}
	@Override
	public void onPlayerCardUsed(Player player) {
		getGameInformation();
		
		sound.playSound("handcard");
	}



	@Override
	public void onPlayerCardDealt(int playerTurn) {
		getGameInformation();
		Log.d(TAG, "onPlayerCardDealt");
	}
}
