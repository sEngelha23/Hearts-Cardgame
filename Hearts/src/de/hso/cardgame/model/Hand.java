package de.hso.cardgame.model;

import com.github.andrewoma.dexx.collection.Set;

/**
 * Die Karten auf der Hand einer Spielerin.
 */
public record Hand(Set<Card> cards) {
	
	public boolean hasCard(Card c) {
        return this.cards.contains(c);
    }
	
}


