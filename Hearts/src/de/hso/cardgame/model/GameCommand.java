package de.hso.cardgame.model;

import com.github.andrewoma.dexx.collection.Map;
import com.github.andrewoma.dexx.collection.Maps;
import com.github.andrewoma.dexx.collection.Set;

import de.hso.cardgame.gamecentral.server.EventConsumer;
import de.hso.cardgame.model.GameEvent.HandsDealt;

import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.ThreadLocalRandom;

import com.github.andrewoma.dexx.collection.ArrayList;
import com.github.andrewoma.dexx.collection.HashSet;
import com.github.andrewoma.dexx.collection.List;

public sealed interface GameCommand 
    permits GameCommand.DealHands, GameCommand.PlayCard, GameCommand.RegisterPlayer
{

    /**
     * Anweisung (von einer Spielerin): ich möchte am Spiel teilnehmen
     */
    public static record RegisterPlayer(String name) implements GameCommand {}
    
    /**
     * Anweisung (von extern): die Karten sollen ausgeteilt werden.
     */
    
    public static record DealHands(Map<Player, Hand> cards) implements GameCommand {
    	public static Map<Player, Hand> generate(Iterable<Player> players) {
    	    Map<Player, Hand> result = Maps.of();

    	    // Karten für ein volles Deck generieren
    	    List<Card> listFullDeck = new ArrayList<>();
    	    for (Suit suit : Suit.values()) {
    	        for (int rankValue = 2; rankValue <= 14; rankValue++) {
    	            listFullDeck = listFullDeck.append(new Card(suit, new Rank(rankValue)));
    	        }
    	    }

    	    // Dexx-Liste in java.util.ArrayList konvertieren und mischen
    	    java.util.ArrayList<Card> javaList = new java.util.ArrayList<>();
    	    for (Card card : listFullDeck) {
    	        javaList.add(card);
    	    }
    	    java.util.Collections.shuffle(javaList);

    	    // Gemischte Karten in ein Dexx-Set konvertieren
    	    Set<Card> fullDeck = HashSet.empty();
    	    for (Card card : javaList) {
    	        fullDeck = fullDeck.add(card);
    	    }

    	    // Karten an Spieler verteilen
    	    Iterator<Card> deckIterator = fullDeck.iterator();
    	    for (Player player : players) {
    	        Set<Card> tempHand = HashSet.empty();
    	        for (int i = 0; i < 13; i++) {
    	            if (deckIterator.hasNext()) {
    	                tempHand = tempHand.add(deckIterator.next());
    	            }
    	        }
    	        result = result.put(player, new Hand(tempHand));
    	    }

    	    return result;
    	}


        }
    


    /**
     * Anweisung (von einer Spielerin): ich (Spielerin player) spiele Karte card aus. 
     */
    public static record PlayCard(Player player, Card card) implements GameCommand {}
}
