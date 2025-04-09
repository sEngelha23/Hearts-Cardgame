package de.hso.cardgame.gamecentral.server;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;

import com.github.andrewoma.dexx.collection.*;
import de.hso.cardgame.model.*;
import de.hso.cardgame.util.Logging;


class GameLogicTest {

    private Hand assembleHand(int[] rs, Suit aceSuit) {
        var builder = Sets.<Card>builder();
        Suit[] allSuits = Suit.values();
        for (Suit s : allSuits) {
            for (int i = 0; i < rs.length; i++) {
                builder.add(new Card(s, new Rank(rs[i])));
            }
        }
        Card ace = new Card(aceSuit, Rank.ACE);
        builder.add(ace);
        return new Hand(builder.build());
    }
    
    private Hand assembleHandAllCards(Suit suit) {
        var builder = Sets.<Card>builder();
        for (Rank r : Rank.values()) {
            builder.add(new Card(suit, r));
        }
        return new Hand(builder.build());
    }
    
    private Stack assembleStack(Suit suit) {
        var builder = Sets.<Card>builder();
        for (int i = 2; i <= 5; i++) {
            Card c = new Card(suit, new Rank(i));
            builder.add(c);
        }
        return new Stack(builder.build());
    }
    
    private GameState sampleState() {
        // Stack: every player has 2,3,4,5 in its stack, P1:diamonds, P2:hearts, P3:spades, P4:clubs
        // Hands:
        // - Every player has ace on its hand, P1:diamonds, P2:hearts, P3:spades, P4:clubs
        // - P1 has 6+7 each suit, P2 has 8+9 each suit, P3 has 10+jack each suite, P4 has queen+king each suite
        var players = Maps.of(Player.P1, "Max", Player.P2, "Kurt", Player.P3, "Clara", Player.P4, "Verena");
        var hands = Maps.of(
                Player.P1, assembleHand(new int[] {6, 7}, Suit.Diamonds),
                Player.P2, assembleHand(new int[] {8, 9}, Suit.Hearts),
                Player.P3, assembleHand(new int[] {10, 11}, Suit.Spades),
                Player.P4, assembleHand(new int[] {12, 13}, Suit.Clubs)
            );
        var stacks = Maps.of(
                Player.P1, assembleStack(Suit.Diamonds),
                Player.P2, assembleStack(Suit.Hearts),
                Player.P3, assembleStack(Suit.Spades),
                Player.P4, assembleStack(Suit.Clubs)
            );
        var state = new GameState(players, hands, stacks, new Trick(LinkedLists.of()), Optional.of(Player.P1));    
        state.validate();
        return state;
        
    }
    
    private Card hearts6 = new Card(Suit.Hearts, new Rank(6));
    private Card hearts8 = new Card(Suit.Hearts, new Rank(8));
    private Card hearts10 = new Card(Suit.Hearts, new Rank(10));
    private Card spades8 = new Card(Suit.Spades, new Rank(10));
    private Card aceOfSpades = new Card(Suit.Spades, Rank.ACE);
    
    @Test
    void testIsPlayValid() {
        var state = sampleState();
        assertTrue(GameLogic.isPlayValid(Player.P1, hearts6, state));
        assertFalse(GameLogic.isPlayValid(Player.P2, hearts8, state)); // it's not the turn of P2
        assertFalse(GameLogic.isPlayValid(Player.P1, hearts8, state)); // P1 does not have hearts 8
        // Now P1 please 6 hearts
        state = state.applyEvent(new GameEvent.CardPlayed(Player.P1, hearts6));
        state = state.applyEvent(new GameEvent.PlayerTurn(Player.P2));
        state.validate();
        assertTrue(GameLogic.isPlayValid(Player.P2, hearts8, state)); // now OK
        assertFalse(GameLogic.isPlayValid(Player.P2, spades8, state)); // can't play spades because P2 still has hearts
        assertFalse(GameLogic.isPlayValid(Player.P3, hearts10, state)); // it's not the turn of P3
    }

    @Test
    void testWhoTakesTrick() {
        var trick1 = new Trick(LinkedLists.of(
                new PlayerCard(Player.P2, hearts8),
                new PlayerCard(Player.P3, hearts6),
                new PlayerCard(Player.P4, aceOfSpades),
                new PlayerCard(Player.P1, hearts10)));
        assertEquals(Player.P1, GameLogic.whoTakesTrick(trick1));
        var trick2 = new Trick(LinkedLists.of(
                new PlayerCard(Player.P2, hearts10),
                new PlayerCard(Player.P3, hearts6),
                new PlayerCard(Player.P4, aceOfSpades),
                new PlayerCard(Player.P1, hearts8)));
        assertEquals(Player.P2, GameLogic.whoTakesTrick(trick2));            
    }

    @Test
    void testFullGame() {
        var log = Logging.getLogger("GameLogicTest");
        log.info("Starting full game test");
        var collector = new EventCollector();
        var logic = new GameLogic(GameState.empty, collector);
        try {
            logic.processCommand(new GameCommand.RegisterPlayer("Paul"));
        } catch(Throwable t) {
            t.printStackTrace();
        }
        logic.processCommand(new GameCommand.RegisterPlayer("Clara"));
        logic.processCommand(new GameCommand.RegisterPlayer("Lotte"));
        logic.processCommand(new GameCommand.RegisterPlayer("Max"));
        // Every player has all cards of one suit
        var hands = Maps.of(
                Player.P1, assembleHandAllCards(Suit.Diamonds),
                Player.P2, assembleHandAllCards(Suit.Hearts),
                Player.P3, assembleHandAllCards(Suit.Spades),
                Player.P4, assembleHandAllCards(Suit.Clubs)
            );
        logic.processCommand(new GameCommand.DealHands(hands));
        // The players first play the 2, then the 3, ...
        for (int r = Rank.MIN_VALUE; r <= Rank.MAX_VALUE; r++) {
            logic.processCommand(new GameCommand.PlayCard(Player.P1, new Card(Suit.Diamonds, new Rank(r))));
            logic.processCommand(new GameCommand.PlayCard(Player.P2, new Card(Suit.Hearts, new Rank(r))));
            logic.processCommand(new GameCommand.PlayCard(Player.P3, new Card(Suit.Spades, new Rank(r))));
            logic.processCommand(new GameCommand.PlayCard(Player.P4, new Card(Suit.Clubs, new Rank(r))));
        }
        log.info("Finished full game test, now asserting events");
        java.util.List<GameEvent> events = collector.getEvents();
        GameEvent last = events.get(events.size() - 1);
        assertTrue(last instanceof GameEvent.GameOver);
        GameEvent.GameOver go = (GameEvent.GameOver)last;
        assertEquals(new Score(Maps.of(Player.P1, 13, Player.P2, 0, Player.P3, 0, Player.P4, 0)), go.score());
    }
}
