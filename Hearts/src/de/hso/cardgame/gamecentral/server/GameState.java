package de.hso.cardgame.gamecentral.server;

import java.lang.Iterable;
import java.util.Arrays;
import java.util.Optional;
import com.github.andrewoma.dexx.collection.*;

import de.hso.cardgame.model.*;
import de.hso.cardgame.model.GameCommand.DealHands;
import de.hso.cardgame.model.GameEvent.CardPlayed;
import de.hso.cardgame.model.GameEvent.HandsDealt;
import de.hso.cardgame.model.GameEvent.PlayerRegistered;
import de.hso.cardgame.model.GameEvent.PlayerTurn;
import de.hso.cardgame.model.GameEvent.TrickTaken;

public record GameState(
        Map<Player, String> playerNames,
        Map<Player, Hand> playerHands,
        Map<Player, Stack> playerStacks,
        Trick currentTrick,
        Optional<Player> nextPlayer 
    ) {
    
    public static final GameState empty = new GameState(Maps.of(), Maps.of(), Maps.of(), Trick.empty, Optional.empty()); 
    
    
    public GameState applyEvent(GameEvent e) {
    	if (e instanceof PlayerRegistered pr) {
    	    if (playerNames.containsKey(pr.player())) {
    	        throw new IllegalStateException("Spieler ist bereits registriert: " + pr.player());
    	    }
    	    if (playerNames.size() >= 4) {
    	        throw new IllegalStateException("Maximale Spieleranzahl erreicht.");
    	    }
    	    Map<Player, String> newMap = this.playerNames().put(pr.player(), pr.name());
    	    return new GameState(newMap, playerHands, playerStacks, currentTrick, nextPlayer);
    	    
    	} else if (e instanceof HandsDealt hd) {
    	        if (!playerNames().containsKey(hd.player())) {
    	            throw new IllegalStateException("Unbekannter Spieler: " + hd.player());
    	            }
    	       
    	        Map<Player, Hand> updatedPlayerHands = playerHands.put(hd.player(), hd.hand());
    	        
    	        return new GameState(playerNames, updatedPlayerHands, playerStacks, currentTrick, nextPlayer);
    	    
    	} else if (e instanceof PlayerTurn pt) {
    		
    			    if (!playerNames().containsKey(pt.player())) {
    			        throw new IllegalStateException("Unbekannter Spieler: " + pt.player());}

    			
    	} else if (e instanceof CardPlayed cp) {
    	    Player player = cp.player();
    	    Card card = cp.card();

    	    Hand currentHand = playerHands().get(player);

    	    Set<Card> updatedCards = HashSet.empty();
    	    for (Card c : currentHand.cards()) {
    	        if (!c.equals(card)) {
    	            updatedCards = updatedCards.add(c);
    	        }
    	        else {
    	        	continue;
    	        }
    	    }
    	    Hand updatedHand = new Hand(updatedCards);
    	    Map<Player, Hand> updatedPlayerHands = playerHands().put(player, updatedHand);

    	    Trick updatedTrick = currentTrick().addCard(player, card);

    	    Player[] players = {Player.P1, Player.P2, Player.P3, Player.P4};
    	    int currentIndex = -1;
    	    for (int i = 0; i < players.length; i++) {
    	        if (players[i].equals(player)) {
    	            currentIndex = i;
    	            break;
    	        }
    	    }

    	    if (currentIndex == -1) {
    	        throw new IllegalStateException("Unbekannter Spieler: " + player);
    	    }

    	    int nextIndex = (currentIndex + 1) % players.length;
    	    Player nextPlayer = players[nextIndex];
		    return new GameState(playerNames, updatedPlayerHands, playerStacks, updatedTrick, Optional.of(nextPlayer));
    	 
    	} else if (e instanceof TrickTaken tt) {
    	    Player winner = tt.player();

    	    Stack currentStack = playerStacks().containsKey(winner)
    	        ? playerStacks().get(winner)
    	        : new Stack(HashSet.empty());

    	    for (PlayerCard pc : tt.trick().cards()) {
    	        currentStack = new Stack(currentStack.cards().add(pc.card()));
    	    }

    	    Map<Player, Stack> updatedPlayerStacks = playerStacks().put(winner, currentStack);

    	    // Der Gewinner beginnt den nächsten Trick
    	    return new GameState(playerNames, playerHands, updatedPlayerStacks, Trick.empty, Optional.of(winner));
    	}




        return this;
    }
}




