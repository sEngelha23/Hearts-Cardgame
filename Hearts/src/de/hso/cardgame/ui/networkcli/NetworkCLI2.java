package de.hso.cardgame.ui.networkcli;

import de.hso.cardgame.model.*;
import de.hso.cardgame.ui.UI;
import de.hso.cardgame.ui.Client;
import de.hso.cardgame.model.GameCommand.PlayCard;
import de.hso.cardgame.model.GameEvent.CardPlayed;
import de.hso.cardgame.model.GameEvent.HandsDealt;
import de.hso.cardgame.model.GameEvent.PlayerRegistered;
import de.hso.cardgame.model.GameEvent.PlayerTurn;
import de.hso.cardgame.model.GameEvent.TrickTaken;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Scanner;

public class NetworkCLI2 implements UI {

    // einfache Verwaltung der Karten auf den Händen der Spieler
    private com.github.andrewoma.dexx.collection.Map<Player, com.github.andrewoma.dexx.collection.Set<Card>> cards 
        = com.github.andrewoma.dexx.collection.Maps.of();
    
    private com.github.andrewoma.dexx.collection.Map<Player, String> playerNames 
        = com.github.andrewoma.dexx.collection.Maps.of();

    private final Client client;
    private Player nextPlayer;

    public NetworkCLI2(Client client) {
        this.client = client;
    }

    public static void main(String[] args) throws UnknownHostException, IOException {
        // 1) Frage Host und Port ab
        Scanner sc = new Scanner(System.in);
        System.out.println("Bitte Host eingeben (z.B. localhost): ");
        String host = sc.nextLine();
        System.out.println("Bitte Port eingeben (z.B. 9003): ");
        int port = Integer.parseInt(sc.nextLine());

        // 2) Erzeuge RemoteClient und verbinde
        RemoteClient2 remoteClient = new RemoteClient2(host, port);
        var ui = new NetworkCLI2(remoteClient);
        remoteClient.setUI(ui);

        // 3) Frage den Spieler-Namen ab und registriere dich beim Server
        System.out.println("Bitte Namen eingeben: ");
        String name = sc.nextLine();
        remoteClient.register(name);

        // 4) Starte die (endlose) Verarbeitungsschleife (in der Oberklasse Client)
        remoteClient.run();
    }

    /**
     * Methoden aus dem UI-Interface
     */
    @Override
    public void updatePlayers(PlayerRegistered pr) {
        // Ein Spieler hat sich registriert.
        System.out.println(pr.name() + " (" + pr.player() + ") joined the game.");
        playerNames = playerNames.put(pr.player(), pr.name());
    }

    @Override
    public void setCards(HandsDealt hd) {
        // Der Server teilt Karten aus.
        System.out.println("Karten für " + hd.player() + " sind angekommen.");
        cards = cards.put(hd.player(), hd.hand().cards());
    }

    @Override
    public void updatePlayerTurn(PlayerTurn pt) {
        this.nextPlayer = pt.player();
        System.out.println("Am Zug: " + playerNames.get(pt.player()));
        
        // Wenn wir selbst an der Reihe sind, können wir eine Karte wählen:
        // (In einer echten CLI würdest du hier evtl. in einer Schleife die Eingabe abfragen.)
        if (cards.get(nextPlayer) != null && !cards.get(nextPlayer).isEmpty()) {
            System.out.println("Eigene Karten:");
            for (Card c : cards.get(nextPlayer)) {
                System.out.println("   " + c);
            }
            System.out.println("Welche Karte spielen? Bitte eingeben (z.B. 'Hearts 2'):");

            Scanner sc = new Scanner(System.in);
            String cardString = sc.nextLine();
            Card chosenCard = Card.fromString(cardString);
            
            // Wenn die Karte in der Hand liegt, spiele sie
            if (chosenCard != null && cards.get(nextPlayer).contains(chosenCard)) {
                this.clientPlayCard(new PlayCard(nextPlayer, chosenCard));
            } else {
                System.out.println("Ungültige Karte oder nicht in deiner Hand!");
            }
        }
    }

    private void clientPlayCard(PlayCard cmd) {
        // Lokale Hand sofort aktualisieren (optional).
        var oldSet = cards.get(cmd.player());
        if (oldSet != null && oldSet.contains(cmd.card())) {
            cards = cards.put(cmd.player(), oldSet.remove(cmd.card()));
        }
        // Sende Befehl an den Server
        if (client instanceof RemoteClient2 rc) {
            rc.sendCommand(cmd); // wir geben eine Hilfsmethode frei
        }
    }

    @Override
    public void updateCardPlayed(CardPlayed cp) {
        // Zeige an, welche Karte gespielt wurde.
        System.out.println(playerNames.get(cp.player()) + " spielte " + cp.card());
    }

    @Override
    public void onTrickTaken(TrickTaken tt) {
        System.out.println(playerNames.get(tt.player()) + " gewinnt den Stich!");
    }

    @Override
    public void endDialog(String msg) {
        System.out.println("Spielende: " + msg);
        // Verbindung schließen
//        client.dispose();
        System.exit(0);
    }

    @Override
    public void dispose() {
        // Nichts weiter nötig hier
    }
}
