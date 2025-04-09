package de.hso.cardgame.model;

import java.util.Optional;

import com.github.andrewoma.dexx.collection.LinkedLists;
import com.github.andrewoma.dexx.collection.List;

public record Trick(List<PlayerCard> cards) {

    public static Trick empty = new Trick(LinkedLists.of());
    
    public Trick addCard(Player p, Card c) {
        return new Trick(this.cards.append(new PlayerCard(p, c)));
    }
    
    public Optional<Suit> leadingSuit() {
        if (cards.size() == 0) {
            return Optional.empty();
        } else {
            return Optional.of(cards.get(0).card().suit());
        }
    }
}
