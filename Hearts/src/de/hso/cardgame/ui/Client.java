package de.hso.cardgame.ui;

import de.hso.cardgame.model.GameEvent;
import de.hso.cardgame.model.GameEvent.CardPlayed;
import de.hso.cardgame.model.GameEvent.GameError;
import de.hso.cardgame.model.GameEvent.GameOver;
import de.hso.cardgame.model.GameEvent.HandsDealt;
import de.hso.cardgame.model.GameEvent.PlayerError;
import de.hso.cardgame.model.GameEvent.PlayerRegistered;
import de.hso.cardgame.model.GameEvent.PlayerTurn;
import de.hso.cardgame.model.GameEvent.TrickTaken;

public abstract class Client {
    private UI ui;
    
    public abstract void register(String name);
    
    public abstract GameEvent readGameEvent();
    
    public void run() {
        boolean gameOver = false;
        
        while (!gameOver) {
            gameOver = handleGameEvent(readGameEvent());
        }
        
        ui.dispose();
    }
    
    /**
     * Handles a game event
     * @return true if game is over, false otherwise
     */
    protected boolean handleGameEvent(GameEvent event) {
        if (event instanceof PlayerRegistered pr) {
            ui.updatePlayers(pr);
        } else if (event instanceof HandsDealt hd) {
            ui.setCards(hd);
        } else if (event instanceof PlayerTurn pt) {
            ui.updatePlayerTurn(pt);
        } else if (event instanceof CardPlayed cp) {
            ui.updateCardPlayed(cp);
        } else if (event instanceof TrickTaken tt) {
            ui.onTrickTaken(tt);
        } else if (event instanceof PlayerError pe) {
            ui.endDialog("Player Error (" + pe.player() + "): " + pe.msg());
            return true;
        } else if (event instanceof GameError ge) {
            ui.endDialog("Game Error: " + ge.msg());
            return true;
        } else if (event instanceof GameOver go) {
            ui.endDialog("Game Over: " + go.score());
            return true;
        }
        return false;
    }

    public void setUI(UI ui) {
        this.ui = ui;
    }
}
