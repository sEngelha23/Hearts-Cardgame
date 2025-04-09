package de.hso.cardgame.gamecentral.server;

import java.util.*;

import de.hso.cardgame.model.GameEvent;

public class EventCollector implements EventConsumer {

    private List<GameEvent> events = new ArrayList<>();
    
    public List<GameEvent> getEvents() {
        return events;
    }
    
    @Override
    public void consumeEvent(GameEvent event) {
        events.add(event);
    }

}
