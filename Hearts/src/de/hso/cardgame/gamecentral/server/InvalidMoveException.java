package de.hso.cardgame.gamecentral.server;

import de.hso.cardgame.model.Player;

public class InvalidMoveException extends RuntimeException {
    private final Player player;

    public InvalidMoveException(String message, Player player) {
        super(message);
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }
}
