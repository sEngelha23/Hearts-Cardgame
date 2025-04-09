package de.hso.cardgame.gamecentral.server;


import com.github.andrewoma.dexx.collection.*;
import java.lang.Iterable;
import java.util.Optional;

import de.hso.cardgame.model.*;
import de.hso.cardgame.model.GameCommand.DealHands;
import de.hso.cardgame.model.GameCommand.RegisterPlayer;
import de.hso.cardgame.model.GameEvent.CardPlayed;
import de.hso.cardgame.model.GameEvent.HandsDealt;
import de.hso.cardgame.model.GameEvent.PlayerRegistered;
import de.hso.cardgame.model.GameEvent.PlayerTurn;
import de.hso.cardgame.model.GameEvent.TrickTaken;

public class GameLogic {
	
	GameState initialState;
	EventConsumer eventConsumer;

    public GameLogic(GameState initialState, EventConsumer eventConsumer) { 
        this.initialState = initialState;
        this.eventConsumer = eventConsumer;
    }
    
    public static boolean isPlayValid(Player player, Card card, GameState state) {
        Hand playerHand = state.playerHands().get(player);


        // Prüfen, ob die Karte in der Hand des Spielers ist
        if (playerHand == null || !playerHand.cards().contains(card)) {
            System.out.println("Ungültiger Zug: Karte nicht in der Hand des Spielers.");
            return false;
        }

        // Prüfen, ob der Spieler am Zug ist
        if (state.nextPlayer().isEmpty() || !state.nextPlayer().get().equals(player)) {
            System.out.println("Ungültiger Zug: Spieler ist nicht am Zug.");
            return false;
        }

        // Prüfen, ob die führende Farbe bedient werden muss
        Optional<Suit> leadingSuit = state.currentTrick().leadingSuit();
        if (leadingSuit.isPresent()) {
            Suit suitToFollow = leadingSuit.get();

            boolean hasLeadingSuit = false;
            for (Card c : playerHand.cards()) {
                if (c.suit() == suitToFollow) {
                    hasLeadingSuit = true;
                    break;
                }
            }

            // Wenn der Spieler die führende Farbe hat, aber eine andere spielt, ist der Zug ungültig
            if (hasLeadingSuit && card.suit() != suitToFollow) {
                System.out.println("Ungültiger Zug: Spieler hat die führende Farbe, spielt aber eine andere.");
                return false;
            }
        }

        // Wenn alle Prüfungen bestanden sind, ist der Zug gültig
        return true;
    }
    
    static Player whoTakesTrick(Trick trick) {
        PlayerCard winningCard = trick.cards().first();
        Suit leadingSuit = trick.leadingSuit().orElseThrow(() ->
            new IllegalStateException("Keine führende Farbe gefunden.")
        );

        for (PlayerCard pc : trick.cards()) {
            if (pc.card().suit().equals(leadingSuit)) {
                if (pc.card().rank().value() > winningCard.card().rank().value()) {
                    winningCard = pc;
                }
            }
        }

        return winningCard.player();
    }


    public void processCommand(GameCommand cmd) {
        if (cmd instanceof RegisterPlayer rp) {
        	 int playerNumber = initialState.playerNames().size();
        	 
        	 if (playerNumber < 4) {
        		 Player player = Player.values()[playerNumber];
        		 
        		 GameEvent playerRegist = new PlayerRegistered(player, rp.name(), initialState.playerNames());
        		 
        		 initialState = initialState.applyEvent(playerRegist);
        		 eventConsumer.consumeEvent(playerRegist); 
        		 if (playerNumber == 3) { 
                     Iterable<Player> players = initialState.playerNames().keys();
                     Map<Player, Hand> hands = DealHands.generate(players);
                     GameCommand commandNeu = new GameCommand.DealHands(hands);
                     processCommand(commandNeu);
                         }

        		      	 
        		}  
        	 
        }
        else if (cmd instanceof DealHands dh) {
            Map<Player, Hand> hands = dh.cards();

            
            for (Player player : hands.keys()) {
                Hand hand = hands.get(player);

                if (hand == null) {
                    throw new IllegalStateException("Keine Hand für Spieler: " + player);
                }
                GameEvent handsDealt = new HandsDealt(player, hand);

                initialState = initialState.applyEvent(handsDealt);
                eventConsumer.consumeEvent(handsDealt);

            }

            if (this.initialState.nextPlayer().isEmpty()) {
                this.initialState = new GameState(
                    initialState.playerNames(),
                    initialState.playerHands(),
                    initialState.playerStacks(),
                    initialState.currentTrick(),
                    Optional.of(Player.P1)
                );
                eventConsumer.consumeEvent(new GameEvent.PlayerTurn(Player.P1));
                
            }
            
        } else if (cmd instanceof GameCommand.PlayCard pc) {
        	Player player = pc.player();
            Card card = pc.card();


            // Überprüfe, ob der Spieler am Zug ist
            boolean valid = isPlayValid(player, card, initialState);
            if (!valid) {
                System.out.println("Ungültiger Zug von Spieler: " + player);
                return;
            }

            GameEvent cardPlayedEvent = new GameEvent.CardPlayed(player, card);
            initialState = initialState.applyEvent(cardPlayedEvent);
            eventConsumer.consumeEvent(cardPlayedEvent);
           
            if (initialState.currentTrick().cards().size() == Player.values().length) {
                Player winner = whoTakesTrick(initialState.currentTrick());
                Trick completedTrick = initialState.currentTrick();

                GameEvent trickTakenEvent = new GameEvent.TrickTaken(winner, completedTrick);
                initialState = initialState.applyEvent(trickTakenEvent);
                eventConsumer.consumeEvent(trickTakenEvent);
            } 
                GameEvent playerTurnEvent = new GameEvent.PlayerTurn(initialState.nextPlayer().orElseThrow());
                initialState = initialState.applyEvent(playerTurnEvent);
                eventConsumer.consumeEvent(playerTurnEvent);

            boolean allHandsEmpty = true;

         for (Hand hand : initialState.playerHands().values()) {
             if (!hand.cards().isEmpty()) {
                 allHandsEmpty = false;
                 break;
             }
         }

         if (allHandsEmpty) {

             Map<Player, Integer> scores = Maps.of(
                 Player.P1, 0,
                 Player.P2, 0,
                 Player.P3, 0,
                 Player.P4, 0
             );

             for (Player playerInGame : initialState.playerStacks().keys()) {
                 Stack stack = initialState.playerStacks().get(playerInGame);
                 int heartsCount = 0;
                 for (Card c : stack.cards()) {
                     if (c.suit() == Suit.Hearts) {
                         heartsCount++;
                     }
                 }
                 scores = scores.put(playerInGame, heartsCount);
             }

             // Debug-Ausgabe der finalen Scores
             System.out.println("Finale Scores: " + new Score(scores));

             // Erstelle und konsumiere ein GameOver-Event
             GameEvent gameOverEvent = new GameEvent.GameOver(new Score(scores));
             initialState = initialState.applyEvent(gameOverEvent);
             eventConsumer.consumeEvent(gameOverEvent);
         		}
         
            }
        }

    
    public GameState getState() {
        return this.initialState;
    }
    
    public EventConsumer getConsumer() {
    	return this.eventConsumer;
    }
    
    }



		
