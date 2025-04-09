package de.hso.cardgame.ui;

import de.hso.cardgame.gamecentral.server.EventConsumer;
import de.hso.cardgame.gamecentral.server.GameLogic;
import de.hso.cardgame.gamecentral.server.GameState;
import de.hso.cardgame.model.GameCommand.DealHands;
import de.hso.cardgame.model.GameCommand.PlayCard;
import de.hso.cardgame.model.GameCommand.RegisterPlayer;
import de.hso.cardgame.model.GameEvent;

public class LocalClient extends Client implements EventConsumer {
    private GameLogic logic;

    public LocalClient() {
        this.logic = new GameLogic(GameState.empty, this);
    }

    @Override
    public void register(String name) {
        logic.processCommand(new RegisterPlayer(name));
    }

    @Override
    public GameEvent readGameEvent() {
        // Not required for a local client
        // Everything will be processed through consumeEvent
        throw new IllegalStateException();
    }

    @Override
    public void consumeEvent(GameEvent event) {
        handleGameEvent(event);
    }
    
    public void playCard(PlayCard card) {
        logic.processCommand(card);
    }
    
    public void dealHands() {
        logic.processCommand(new DealHands(DealHands.generate(logic.getState().playerNames().keys())));
    }

}
