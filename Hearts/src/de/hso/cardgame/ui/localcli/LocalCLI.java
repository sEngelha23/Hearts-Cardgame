package de.hso.cardgame.ui.localcli;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Scanner;

import com.github.andrewoma.dexx.collection.Map;
import com.github.andrewoma.dexx.collection.Maps;
import com.github.andrewoma.dexx.collection.Set;
import de.hso.cardgame.model.Card;
import de.hso.cardgame.model.Player;
import de.hso.cardgame.model.GameCommand.PlayCard;
import de.hso.cardgame.model.GameEvent.CardPlayed;
import de.hso.cardgame.model.GameEvent.HandsDealt;
import de.hso.cardgame.model.GameEvent.PlayerRegistered;
import de.hso.cardgame.model.GameEvent.PlayerTurn;
import de.hso.cardgame.model.GameEvent.TrickTaken;
import de.hso.cardgame.ui.LocalClient;
import de.hso.cardgame.ui.UI;

public class LocalCLI implements UI {
    private Map<Player, Set<Card>> cards = Maps.of();
    private Map<Player, String> names = Maps.of();
    private Scanner in;
    private LocalClient client;
    private Player nextPlayer;

    public LocalCLI(Scanner in, LocalClient client) {
        this.in = in;
        this.client = client;
    }

    public static void main(String[] args) throws UnknownHostException, IOException {
        var client = new LocalClient();
        var in = new Scanner(System.in);
        var ui = new LocalCLI(in, client);
        client.setUI(ui);

        System.out.println("Please enter the player's names.");
        for (int i = 0; i < 4; i++) {
            System.out.print("Player " + i + ": ");
            client.register(in.nextLine());
        }
        


        ui.run();
    }
    
    public void run() {
        String cardString;
        do {
            var currentPlayer = nextPlayer;
            var cards = this.cards.get(currentPlayer);
            printCards(currentPlayer);
            System.out.println("Enter the card to play, e.g. \"Hearts 2\": ");
            cardString = in.nextLine();
            var card = Card.fromString(cardString);
            if (card != null) {
                if (cards.contains(card)) {
                    client.playCard(new PlayCard(currentPlayer, card));
                    this.cards = this.cards.put(currentPlayer, cards.remove(card));
                } else {
                    System.err.println(cardString + " is not in your hand.");
                }
            } else {
                System.err.println("Invalid format for card. Input something like \"Hearts 6\" or \"Spades Ace\"");
            }
        } while (!cardString.toLowerCase().equals("exit"));
        
        in.close();
    }
    
    @Override
    public void dispose() {}

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
        System.out.println("Player Turn: " + names.get(pt.player()));
        nextPlayer = pt.player();
    }

    @Override
    public void updateCardPlayed(CardPlayed cp) {
        System.out.println(names.get(cp.player()) + " played " + cp.card().toString());
    }

    @Override
    public void onTrickTaken(TrickTaken tt) {
        System.out.println(names.get(tt.player()) + " takes the trick.");
    }

    @Override
    public void endDialog(String string) {
        System.out.println(string);
        System.exit(0);
    }
    
    private void printCards(Player p) {
        System.out.println(names.get(p) + "'s cards:");
        for (var card : cards.get(p)) {
            System.out.println(card.toString());
        }
    }
}
