package de.hso.cardgame.ui;

import de.hso.cardgame.model.GameEvent.CardPlayed;
import de.hso.cardgame.model.GameEvent.HandsDealt;
import de.hso.cardgame.model.GameEvent.PlayerRegistered;
import de.hso.cardgame.model.GameEvent.PlayerTurn;
import de.hso.cardgame.model.GameEvent.TrickTaken;

public interface UI {

    void updatePlayers(PlayerRegistered pr);
    void setCards(HandsDealt hd);
    void updatePlayerTurn(PlayerTurn pt);
    void updateCardPlayed(CardPlayed cp);
    void onTrickTaken(TrickTaken tt);
    void endDialog(String string);
    
    void dispose();

}
