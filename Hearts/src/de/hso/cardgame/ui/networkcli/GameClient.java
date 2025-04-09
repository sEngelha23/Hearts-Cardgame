package de.hso.cardgame.ui.networkcli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

import de.hso.cardgame.gamecentral.server.GameCommandSerializer;
import de.hso.cardgame.gamecentral.server.GameEventSerializer;
import de.hso.cardgame.model.Card;
import de.hso.cardgame.model.GameCommand.PlayCard;
import de.hso.cardgame.model.GameCommand.RegisterPlayer;
import de.hso.cardgame.model.GameEvent;
import de.hso.cardgame.model.GameEvent.PlayerTurn;
import de.hso.cardgame.model.Player;

public class GameClient {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 127;

    public static void main(String[] args) {
        try (
            Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            Scanner scanner = new Scanner(System.in)
        ) {
            System.out.println("Verbindung zum Server hergestellt.");

            while (true) {
                String line = in.readLine();
                if (line == null) {
                    System.out.println("Server hat die Verbindung geschlossen.");
                    break;
                }

                switch (line) {
                    case "NAME_PROMPT" -> {
                        System.out.print("Bitte geben Sie Ihren Namen ein: ");
                        String playerName = scanner.nextLine();
                        RegisterPlayer cmd = new RegisterPlayer(playerName);
                        out.println(GameCommandSerializer.toJSON(cmd));
                    }
                    case "DEINE_HAND" -> {
                        System.out.println("=== Deine Handkarten ===");
                        // Lies Zeilen, bis z.B. "AKTUELLER_STICH", "INVALID_MOVE" oder JSON
                        while (true) {
                            in.mark(2048);
                            String next = in.readLine();
                            if (next == null) {
                                System.out.println("Verbindung beendet.");
                                return;
                            }
                            if ("AKTUELLER_STICH".equals(next) 
                                || "INVALID_MOVE".equals(next)
                                || next.startsWith("{")
                                || "GAME_OVER".equals(next)) {
                                in.reset();
                                break;
                            }
                            System.out.println("Karte: " + next);
                        }
                    }
                    case "AKTUELLER_STICH" -> {
                        String trick = in.readLine();
                        System.out.println("Aktueller Stich: " + trick);
                    }
                    case "GAME_OVER" -> {
                        System.out.println("Spiel ist vorbei!");
                        String result = in.readLine();
                        if (result != null) {
                            System.out.println(result);
                        }
                        break; // raus
                    }
                    case "INVALID_MOVE" -> {
                        // Server sagt: letzter Zug war ungültig
                        String reason = in.readLine();
                        System.out.println("FEHLERHAFTER ZUG: " + reason);
                    }
                    default -> {
                        // Prüfen, ob JSON-Event
                        if (line.startsWith("{")) {
                            GameEvent ev = GameEventSerializer.fromJSON(line);
                            if (ev instanceof PlayerTurn pt) {
                                System.out.println("Du bist am Zug: " + pt.player());
                                System.out.print("Welche Karte willst du spielen? (z.B. Hearts 8): ");
                                String cardStr = scanner.nextLine();
                                Card card = Card.fromString(cardStr);

                                PlayCard pc = new PlayCard(pt.player(), card);
                                out.println(GameCommandSerializer.toJSON(pc));
                            } else {
                                System.out.println("Event: " + ev);
                            }
                        } else {
                            // Nur normaler Text
                            System.out.println("Server: " + line);
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Client-Fehler: " + e.getMessage());
        }
    }
}
