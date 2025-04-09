package de.hso.cardgame.model;

import java.util.HashSet;
import java.util.Set;

import com.github.andrewoma.dexx.collection.LinkedLists;
import com.github.andrewoma.dexx.collection.List;

public record Card(Suit suit, Rank rank) {
    public static List<Card> wholeDeck() {
        Suit[] allSuits = Suit.values();
        List<Rank> allRanks = Rank.values();
        var builder = LinkedLists.<Card>builder();
        for (Suit s : allSuits) {
            for (Rank r : allRanks) {
                builder.add(new Card(s, r));
            }
        }
        return builder.build();
    }
    
    @Override
    public String toString() {
        return suit.toString() + " " + rank.toString();
    }

    public static Card fromString(String cardString) {
        var parsed = cardString.split(" ");
        if (parsed.length != 2) {
            return null;
        }
        Suit suit;
        try {
            suit = Suit.valueOf(parsed[0]);
        } catch (IllegalArgumentException e) {
            return null;
        }
        Rank rank = Rank.fromString(parsed[1]);
        if (rank == null) {
        	;
            return null;
        }
        return new Card(suit, rank);
    }
    public static void main(String[] args) {
    	
    }
}
