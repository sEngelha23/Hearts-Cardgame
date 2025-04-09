package de.hso.cardgame.ui.networkcli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import de.hso.cardgame.gamecentral.server.GameCommandSerializer;
import de.hso.cardgame.gamecentral.server.GameEventSerializer;
import de.hso.cardgame.model.Card;
import de.hso.cardgame.model.GameCommand.PlayCard;
import de.hso.cardgame.model.GameCommand.RegisterPlayer;
import de.hso.cardgame.model.GameEvent;
import de.hso.cardgame.model.GameEvent.PlayerTurn;
import de.hso.cardgame.model.Player;
import de.hso.cardgame.model.Rank;
import de.hso.cardgame.model.Suit;

/**
 * KI-Client:
 *  - Schickt seinen Namen "KIClient" auf NAME_PROMPT
 *  - Liest Handkarten nach "DEINE_HAND" Zeile für Zeile.
 *    Da der Server sie als "Server: Spades Rank[10]" usw. schickt,
 *    parse wir genau diese Zeilen.
 *  - Bei PlayerTurn schickt er automatisch einen PlayCard-Befehl.
 */
public class AIClient
{
    private static final String HOST = "localhost";
    private static final int PORT = 127;

    /**
     * Unsere Handkarten als Strings, z.B. "Spades Rank[10]" oder "Hearts Ace".
     */
    private static List<String> myHandStrings = new ArrayList<>();
    private static String momentanerTrick;
    /**
     * Welcher Player wir sind (z.B. P1), sobald wir es aus dem PlayerRegistered-Event erfahren.
     */

    public static void main(String[] args) {
        try (
            Socket socket = new Socket(HOST, PORT);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            System.out.println("[KI] Verbunden mit " + HOST + ":" + PORT);

            while (true) {
                String line = in.readLine();
                if (line == null) {
                    System.out.println("[KI] => Server hat Verbindung beendet.");
                    break;
                }
                switch (line) {
                    case "NAME_PROMPT" -> {
                        // Der Server fragt unseren Namen -> Sende RegisterPlayer
                        RegisterPlayer cmd = new RegisterPlayer("KIClient");
                        out.println(GameCommandSerializer.toJSON(cmd));
                        out.flush();
                        System.out.println("[KI] => Registriere mich als KIClient");
                    }

                    case "DEINE_HAND" -> {
                        System.out.println("Deine Hand:");
                    }

                    case "AKTUELLER_STICH" -> {
                        // Nächste Zeile enthält den Stich als Text
                        String trick = in.readLine();
                        System.out.println("[KI] => Aktueller Stich: " + trick);
                        momentanerTrick = trick; 
                        
                    }

                    case "GAME_OVER" -> {
                        System.out.println("[KI] => Spiel ist vorbei!");
                        // ggf. Ergebniszeile
                        String result = in.readLine();
                        if (result != null) {
                            System.out.println("Ergebnis: " + result);
                        }
                        break;
                    }

                    case "INVALID_MOVE" -> {
                        // Letzter Zug war ungültig
                        String reason = in.readLine();
                        System.out.println("[KI] => Ungültiger Zug: " + reason);
                    }

                    default -> {
                        // Prüfen, ob JSON-Event
                        if (line.startsWith("{")) {
                            GameEvent ev = GameEventSerializer.fromJSON(line);
                            handleEvent(ev, out);
                        } else {
                            System.out.println("Server: " + line);
                            if (!line.startsWith("Verbunden")) {
                                myHandStrings.add(line);
                                
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("[KI] Fehler: " + e.getMessage());
        }
    }

    /**
     * Liest so lange Zeilen, bis wir auf ein Schlüsselwort treffen:
     * - "AKTUELLER_STICH"
     * - "GAME_OVER"
     * - "INVALID_MOVE"
     * - JSON ("{")
     * - null => Ende
     *
     * Jede Zeile, die mit "Server: " anfängt, interpretieren wir als mögliche Kartenzeile.
     * Dann extrahieren wir "Spades Rank[10]", transformieren es ggf. und speichern in myHandStrings.
     */
    private static void readHandLines(BufferedReader in) throws IOException {
        while (true) {
            in.mark(2048);
            String line = in.readLine();
            if (line == null) {
                // Verbindung beendet
                return;
            }
            // Abbruch, wenn Schlüsselwörter oder JSON
            if (line.equals("AKTUELLER_STICH")
                || line.equals("GAME_OVER")
                || line.equals("INVALID_MOVE")
                || line.startsWith("{")) {
                in.reset();
                break;
            }

            // Der Server schickt die Kartenzeilen i.d.R. mit "Server: Spades Queen" etc.
            if (line.startsWith("Server: ")) {
                // => "Server: Spades Queen"
                String cardStr = line.substring("Server: ".length()).trim();
                System.out.println("   -> Karte erkannt: " + cardStr);
            } else {
                // Sonstige Zeile (z.B. "=== Deine Handkarten ===") -> ignorieren oder break
                if (line.startsWith("===")) {
                    // Nur Info
                } else {
                    // Unerwartete Zeile -> reset & break
                    in.reset();
                    break;
                }
            }
        }
    }

    /**
     * Verarbeitung eingehender JSON-Events.
     */
    private static void handleEvent(GameEvent ev, PrintWriter out) {
        if (ev instanceof GameEvent.PlayerRegistered pr) {
            Player myPlayer = pr.player();
            System.out.println("[KI] => Ich bin " + myPlayer);
        } else if (ev instanceof PlayerTurn pt) {
            String suit = null;
            Player myPlayer = pt.player();
            System.out.println("[KI] => Ich bin am Zug!");
            if (!myHandStrings.isEmpty()) {
                if (momentanerTrick.equals("Trick[cards=Nil()]") && !myHandStrings.isEmpty()) {
                    String chosen = myHandStrings.get(0);
                    myHandStrings.clear();
                    System.out.println("[KI] => Spiele: " + chosen);

                    // transform "Rank[10]" => "10"
                    Card c = parseCardString(chosen);
                    if (c == null) {
                        System.out.println("[KI] => Konnte " + chosen + " nicht parsen!");
                        return;
                    }
                    PlayCard cmd = new PlayCard(myPlayer, c);
                    out.println(GameCommandSerializer.toJSON(cmd));
                    out.flush();
                } else if (!momentanerTrick.equals("Trick[cards=Nil()]")) {
                    String regex = "card=(Diamonds|Spades|Clubs|Hearts)";
                    Pattern pattern = Pattern.compile(regex);
                    Matcher matcher = pattern.matcher(momentanerTrick);
                    if (matcher.find()) {
                        suit = matcher.group(1);
                    }

                    Card c = null;
                    PlayCard cmd = null;
                    String stringie = "";
                    for (int i = 0; i < myHandStrings.size(); i++) {
                        stringie = myHandStrings.get(i);
                        if (stringie.startsWith(suit) && suit != null) {
                            c = parseCardString(stringie);
                            break;
                        }
                    }
                    if (c != null) {
                        cmd = new PlayCard(myPlayer, c);  
                    } else {
                    	String chosen = myHandStrings.get(0);
                    	c = parseCardString(chosen);
                    	cmd = new PlayCard(myPlayer, c);
                    	
                    }
                    myHandStrings.clear();
                    System.out.println("[KI] => Spiele: " + c);
                    out.println(GameCommandSerializer.toJSON(cmd));
                    out.flush();
                } else {
                    System.out.println("[KI] => Keine Karten mehr?");
                }
            }
        } else {
            System.out.println("[KI] => Event: " + ev);
        }
    }

    /**
     * Falls dein Card.fromString(...) kein "Spades Rank[10]" akzeptiert,
     * sondern "Spades 10", wandeln wir es hier um.
     */
    private static Card parseCardString(String raw) {
        // Ersetzt "Rank[10.0]" => "10", "Rank[11]" => "11", usw.
        String finalStr = raw.replaceAll("Rank\\[(\\d+)(\\.0)?\\]", "$1");
        // Jetzt z.B. "Spades 10" -> Card.fromString("Spades 10")

        return Card.fromString(finalStr);
    }
}
