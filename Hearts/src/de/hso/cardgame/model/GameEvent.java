package de.hso.cardgame.model;


import java.util.Optional;

import com.github.andrewoma.dexx.collection.*;



public sealed interface GameEvent
    permits GameEvent.PlayerRegistered, GameEvent.HandsDealt, GameEvent.PlayerTurn, GameEvent.CardPlayed, 
        GameEvent.TrickTaken, GameEvent.GameError, GameEvent.PlayerError, GameEvent.GameOver
{

    /**
     * Wenn diese Methode einen Player-Wert liefert, wird das Event nur an diese Spielerin
     * ausgeliefert. Ansonsten bekommen alle das Event zugestellt.
     */
    default Optional<Player> eventTarget() {
        return Optional.empty();
    }
    
    /**
     * Ereignis: Spielerin player hat sich unter name registriert.
     * otherPlayers sind die bisher registrieren Spielerinnen.
     */
    public static record PlayerRegistered(Player player, String name, Map<Player, String> otherPlayers) implements GameEvent {}
    
    /**5
     * Ereignis: Karten h wurden an Spielerin player ausgeteilt. 
     */
    public static record HandsDealt(Player player, Hand hand) implements GameEvent {
    	@Override
    	public Optional<Player> eventTarget() {
    		return Optional.of(player);
    	}
    }
    
    /**
     * Ereignis: Spielerin player ist an der Reihe.
     */
    public static record PlayerTurn(Player player) implements GameEvent {}
    
    /**
     * Ereignis: Spielerin player hat Karte card gespielt.
     */
    public static record CardPlayed(Player player, Card card) implements GameEvent {}
    
    /**
     * Ereignis: Spielerin player hat Stich trick aufgenommen.
     */
    public static record TrickTaken(Player player, Trick trick) implements GameEvent {}
    
    /**
     * Ereignis: Ein Fehler ist aufgetreten, keine bestimmte Spielerin hat 
     * Schuld an diesem Fehler.
     */
    public static record GameError(String msg) implements GameEvent {}

    /**
     * Ereignis: Ein Fehler ist aufgetreten, eine bestimmte Spielerin hat 
     * Schuld an diesem Fehler.
     */
    public static record PlayerError(Player player, String msg) implements GameEvent {}

    /**
     * Ereignis: Spiel zu Ende.
     */
    public static record GameOver(Score score) implements GameEvent {}
}






