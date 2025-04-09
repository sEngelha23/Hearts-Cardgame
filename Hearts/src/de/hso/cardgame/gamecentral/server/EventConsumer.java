package de.hso.cardgame.gamecentral.server;

import de.hso.cardgame.model.GameEvent;

public interface EventConsumer {

    void consumeEvent(GameEvent event);
}
