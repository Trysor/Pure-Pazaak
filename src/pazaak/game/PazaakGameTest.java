package pazaak.game;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class PazaakGameTest {
	
	PazaakGame game;
	
	@Before
	public void before() {
		game = new PazaakGame(false); // create new game for each test
	}
	

	@Test
	public void initiateGameTest() {
		assertNotNull(game.getPlayer(0));
		assertNotNull(game.getPlayer(1));
		
		assertTrue(game.getPlayerTurn() > 0 && game.getPlayerTurn() <= 2); // RNG check
	}
	
	@Test
	public void playerInitTest() {
		assertEquals(0, game.getPlayer(0).getSum());
		assertEquals(0, game.getPlayer(0).getSetsWon());
		assertEquals(0, game.getPlayer(1).getSum());
		assertEquals(0, game.getPlayer(1).getSetsWon());	
	}
	
	
	

	@Test
	public void dealerCardValueTest() {
		Card card1 = new DealerCard(3); // works
		assertEquals(3, card1.getValue());
		Card card2 = new DealerCard(-1); // shouldn't
		assertEquals(999, card2.getValue());
		Card card3 = new DealerCard(11); // shouldn't
		assertEquals(999, card3.getValue());
		Card card4 = new DealerCard(4); 
		assertEquals(4, card4.getValue());
		Card card5 = new DealerCard(0); // shouldn't
		assertEquals(999, card5.getValue());
		Card card6 = new DealerCard(1); // min
		assertEquals(1, card6.getValue());
		Card card7 = new DealerCard(10); // max
		assertEquals(10, card7.getValue());
	}
	
	@Test
	public void handCardValueTest() { 
		Card card = new HandCard(CardNames.TIEBREAKER, 4);
		//assertEquals(999, card.getValue()); // bogus
		card = new HandCard(CardNames.TIEBREAKER, -4);
		//assertEquals(999, card.getValue()); // bogus
		card = new HandCard(CardNames.TIEBREAKER, 1);
		assertEquals(1, card.getValue()); // true
		
		card = new HandCard(CardNames.FLIPPABLE, 7);
		//assertEquals(999, card.getValue()); // bogus
		card = new HandCard(CardNames.FLIPPABLE, -7);
		//assertEquals(999, card.getValue()); // bogus
		card = new HandCard(CardNames.FLIPPABLE, 1);
		assertEquals(1, card.getValue()); // true
		card = new HandCard(CardNames.FLIPPABLE, -1);
		assertEquals(-1, card.getValue()); // true	
		card = new HandCard(CardNames.FLIPPABLE, -2);
		assertEquals(-2, card.getValue()); // true	
		card = new HandCard(CardNames.FLIPPABLE, 2);
		assertEquals(2, card.getValue()); // true	
		
		card = new HandCard(CardNames.TWO_AND_FOUR, 6544645); // value always = 0
		//assertEquals(0, card.getValue()); // card is bogus, but functional.
		card = new HandCard(CardNames.FLIPPABLE, 1);
		assertEquals(1, card.getValue()); // true
		card = new HandCard(CardNames.FLIPPABLE, 2);
		assertEquals(2, card.getValue()); // true	
		card = new HandCard(CardNames.FLIPPABLE, 3);
		assertEquals(3, card.getValue()); // true	
		card = new HandCard(CardNames.FLIPPABLE, 4);
		assertEquals(4, card.getValue()); // true	
		card = new HandCard(CardNames.FLIPPABLE, 5);
		assertEquals(5, card.getValue()); // true
		card = new HandCard(CardNames.FLIPPABLE, 6);
		assertEquals(6, card.getValue()); // true
	}
	
	
	@Test
	public void cardToyingTest() {
		for (int i = 0; i < 1000; i++) { // better way to test this? its random..
			HandCard hand = game.createHandCard();
			DealerCard dealer = new DealerCard();
			
			int origValHand = hand.getValue();
			int origValDealer = dealer.getValue();
			
			dealer.flipValue(); // test whether a card can be flipped properly.
			assertTrue((origValDealer != dealer.getValue()) && (Math.abs(dealer.getValue()) == origValDealer));
			dealer.flipValue();
			assertTrue(origValDealer == dealer.getValue());
			
			
			assertFalse(hand.isUsed());
			
			if (hand.isFlippableCard()) {
				hand.flipValue();
				assertTrue(origValHand != hand.getValue()); // should've changed now
				hand.flipValue();
				assertTrue(origValHand == hand.getValue()); // should've changed back to orig
				
			} else if (hand.getValue() != 0) { // its not flippable. Can't be flipped until actually played.
				hand.flipValue();
				assertTrue(origValHand == hand.getValue()); // shouldn't change. It hasn't been used yet.
				
				hand.setUsed(); // use card
				
				hand.flipValue();
				assertTrue(origValHand != hand.getValue()); // should've changed now
				
				hand.flipValue();
				assertTrue(origValHand == hand.getValue()); // should've changed back to orig
			}


			hand.setUsed();
			assertTrue(hand.isUsed());
			
		}
	}
	
	
	
	@Test
	public void playerGameplaySumTest() {		
		for (int i = 0; i < 1000; i++) { // iterative test
			//Player player = new Player();
			//int sum = 0;
			 

			// remember tests for flipper (2&4, 3&6) cards
		}
	}
	
	
	@Test
	public void playerGameplayStackTest() {
		for (int i = 0; i < 1000; i++) {
			game = new PazaakGame(false); // create new game for each iteration
			Player p1 = game.getPlayer(0);
			Player p2 = game.getPlayer(1);
			int stack1 = 0;
			int stack2 = 0;
			assertEquals(stack1, p1.getStackSize());
			assertEquals(stack2, p2.getStackSize());
			
			
			/*  Needs to be done intelligently. It bugs out for the wrong reasons if done straight forward.
			 *  take into consideration:
			 *  	- Stack sum may go beyond 20 (thus someone wins)
			 *  	- Hand Card interaction may cause bugs. Test that too
			 *  
			 */
		}
	}
	
	
	@Test
	public void playerStackTooBigTest() {
		Player p1 = game.getPlayer(0);
		Player p2 = game.getPlayer(1);
		int p1s = 0;
		int p2s = 0;
		assertEquals(p1s, p1.getStackSize());
		assertEquals(p2s, p2.getStackSize());
		
		/*  Needs to be done intelligently. It bugs out for the wrong reasons if done straight forward.
		 *  take into consideration:
		 *  	- Stack sum may go beyond 19 (thus someone wins)
		 *  	- Hand Card interaction may cause bugs. Test that too
		 *  
		 */
		

		/*
		p1.useCard(1); // Attempting to use card after 9 placed cards (should fail)
		assertEquals(p1s, p1.getStackSize()); // shouldn't change shit.
		
		p2.startRound(); // start round 10 (should fail)
		assertEquals(p2s, p2.getStackSize()); // shouldn't change shit.
		*/
	}


}
