package de.hso.cardgame.ui.networkcli;

import java.io.IOException;
import java.util.Scanner;

import com.github.andrewoma.dexx.collection.Map;
import com.github.andrewoma.dexx.collection.Maps;
import com.github.andrewoma.dexx.collection.Set;

import de.hso.cardgame.model.Card;
import de.hso.cardgame.model.GameCommand.PlayCard;
import de.hso.cardgame.model.GameEvent;
import de.hso.cardgame.model.GameEvent.*;
import de.hso.cardgame.model.Player;
import de.hso.cardgame.ui.UI;

public class NetworkCLI implements UI {
    public class GameServer {

	}

	private Map<Player, Set<Card>> cards = Maps.of();
    private Map<Player, String> names = Maps.of();
    private Scanner in;
    private RemoteClient client;
    private Player nextPlayer;

    public NetworkCLI(Scanner in, RemoteClient client) {
        this.in = in;
        this.client = client;
    }

    public static void main(String[] args) throws IOException {
        Scanner in = new Scanner(System.in);

        System.out.println("Bitte gib die Server-Adresse ein:");
        String host = in.nextLine();

        // Erzeuge den RemoteClient, der die Verbindung zum Server herstellt
        RemoteClient client = new RemoteClient(host, 12345);

        // Erzeuge dieses CLI und setze die UI im Client
        NetworkCLI ui = new NetworkCLI(in, client);
        client.setUI(ui);

        // Spieler registrieren (analog zum LocalCLI)
        System.out.println("Bitte gib die 4 Spielernamen ein:");
        for (int i = 0; i < 4; i++) {
            System.out.print("Player " + i + ": ");
            String name = in.nextLine();
            client.register(name);
        }

        ui.run();
    }

    public void run() {
        String cardString;
        do {
            // Warte auf Events vom Server
            GameEvent event = client.readGameEvent();
            // Verarbeitung des Events über handleGameEvent (aus Basisklasse Client)


            // Analog zum LocalCLI: Wenn wir einen PlayerTurn haben, 
            // dann hat updatePlayerTurn(nextPlayer) nextPlayer gesetzt.

            if (nextPlayer != null) {
                // Prompt analog zum LocalCLI
                System.out.println("Enter the card to play, e.g. \"Hearts 2\" or type 'exit' to quit:");
                cardString = in.nextLine();
                if ("exit".equalsIgnoreCase(cardString)) {
                    // Der Benutzer bricht das Spiel ab
                    break;
                }

                // Versuche, die Karte einzulesen
                Card card = Card.fromString(cardString);
                if (card == null) {
                    System.err.println("Invalid format for card. Input something like \"Hearts 6\" or \"Spades Ace\"");
                    continue;
                }

                // Prüfe, ob die Karte in der Hand existiert
                Set<Card> currentPlayerCards = cards.get(nextPlayer);
                if (currentPlayerCards == null || !currentPlayerCards.contains(card)) {
                    System.err.println(cardString + " is not in your hand.");
                    continue;
                }

                // Sende ein PlayCard-Command über den RemoteClient
                client.sendCommand(new PlayCard(nextPlayer, card));

                // Entferne die Karte lokal aus der Hand
                cards = cards.put(nextPlayer, currentPlayerCards.remove(card));
            } else {
                // Kein nextPlayer gesetzt => wir warten auf das nächste Event
                cardString = null;
            }
        } while (true);

        in.close();
    }

    @Override
    public void dispose() {
        in.close();
    }

    // -------------------------------
    // Implementierungen aus UI
    // -------------------------------

    @Override
    public void updatePlayers(PlayerRegistered pr) {
        System.out.println(pr.name() + " (" + pr.player().toString() + ") joined the game");
        names = names.put(pr.player(), pr.name());
    }

    @Override
    public void setCards(HandsDealt hd) {
        this.cards = this.cards.put(hd.player(), hd.hand().cards());
    }

    @Override
    public void updatePlayerTurn(PlayerTurn pt) {
        System.out.println("Player Turn: " + getPlayerName(pt.player()));
        nextPlayer = pt.player();
    }

    @Override
    public void updateCardPlayed(CardPlayed cp) {
        System.out.println(getPlayerName(cp.player()) + " played " + cp.card().toString());
    }

    @Override
    public void onTrickTaken(TrickTaken tt) {
        System.out.println(getPlayerName(tt.player()) + " takes the trick.");
    }

    @Override
    public void endDialog(String string) {
        System.out.println(string);
        System.exit(0);
    }

    // Hilfsmethode zum Anzeigen von Spielernamen
    private String getPlayerName(Player p) {
        String name = names.get(p);
        if (name == null) {
            return p.toString();
        }
        return name;
    }
}


// package de.hso.cardgame.ui.networkcli;
//
//import java.io.IOException;
//import java.util.Scanner;
//
//import de.hso.cardgame.model.GameEvent;
//import de.hso.cardgame.model.GameEvent.*;
//import de.hso.cardgame.ui.UI;
//
//public class NetworkCLI implements UI {
//    private Scanner in;
//    private RemoteClient client;
//
//    public NetworkCLI(Scanner in, RemoteClient client) {
//        this.in = in;
//        this.client = client;
//    }
//
//    public static void main(String[] args) throws IOException {
//        Scanner in = new Scanner(System.in);
//        System.out.print("Server-Adresse: ");
//        String host = in.nextLine();
//        
//        RemoteClient client = new RemoteClient(host, 12345);
//        NetworkCLI ui = new NetworkCLI(in, client);
//        client.setUI(ui);
//
//        System.out.print("Gib deinen Namen ein: ");
//        String name = in.nextLine();
//        client.register(name);
//
//        ui.run();
//    }
//
//    public void run() {
//        while (true) {
//            GameEvent event = client.readGameEvent();
//            	handleGameEvent(event);
//        }
//    }
//
//    @Override
//    public void updatePlayers(PlayerRegistered pr) {
//        System.out.println(pr.name() + " ist beigetreten.");
//    }
//
//    @Override
//    public void setCards(HandsDealt hd) {
//        System.out.println("Karten für " + hd.player() + ": " + hd.hand());
//    }
//
//    @Override
//    public void updatePlayerTurn(PlayerTurn pt) {
//        System.out.println("Spieler am Zug: " + pt.player());
//    }
//
//    @Override
//    public void updateCardPlayed(CardPlayed cp) {
//        System.out.println(cp.player() + " spielte " + cp.card());
//    }
//
//    @Override
//    public void onTrickTaken(TrickTaken tt) {
//        System.out.println(tt.player() + " hat den Stich gewonnen.");
//    }
//
//    @Override
//    public void endDialog(String message) {
//        System.out.println(message);
//        System.exit(0);
//    }
//
//    @Override
//    public void dispose() {
//        in.close();
//    }
//}
//
