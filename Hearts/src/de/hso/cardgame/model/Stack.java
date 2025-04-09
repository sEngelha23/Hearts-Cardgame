package de.hso.cardgame.model;

import com.github.andrewoma.dexx.collection.Set;

/**
 * Die während des Spiels erzielten Karten einer Spielerin.
 */
public record Stack(Set<Card> cards) {

}
