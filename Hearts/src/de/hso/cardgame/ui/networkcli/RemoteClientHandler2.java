package de.hso.cardgame.ui.networkcli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;

import de.hso.cardgame.gamecentral.server.EventCollector;
import de.hso.cardgame.gamecentral.server.GameCommandSerializer;
import de.hso.cardgame.gamecentral.server.GameEventSerializer;
import de.hso.cardgame.gamecentral.server.GameLogic;
import de.hso.cardgame.gamecentral.server.InvalidMoveException;
import de.hso.cardgame.model.Card;
import de.hso.cardgame.model.GameCommand;
import de.hso.cardgame.model.GameEvent;
import de.hso.cardgame.model.GameEvent.GameOver;
import de.hso.cardgame.model.GameEvent.HandsDealt;
import de.hso.cardgame.model.GameEvent.PlayerRegistered;
import de.hso.cardgame.model.GameEvent.PlayerTurn;
import de.hso.cardgame.model.Hand;
import de.hso.cardgame.model.Player;

/**
 * Jeder Client-Verbindung hat ihren eigenen Thread.
 * Dieser Thread liest fortlaufend Commands vom Client
 * und verteilt die entstehenden Events an die richtigen Player-Sockets.
 */
public class RemoteClientHandler2 extends Thread {
    private static final Map<Player, RemoteClientHandler2> playerHandlers = new HashMap<>();
    // => So können wir z.B. "playerHandlers.get(p)" aufrufen, um dem Player p etwas zu senden.

    private final Socket socket;
    private final GameLogic logic;

    // Welcher Spieler steckt hinter dieser Verbindung?
    // Wird gesetzt, sobald wir ein PlayerRegistered-Event bekommen haben.
    private Player myPlayer = null;

    public RemoteClientHandler2(Socket socket, GameLogic logic) {
        this.socket = socket;
        this.logic = logic;
    }

    @Override
    public void run() {
        try {
            serveClient(socket);
        } catch (IOException e) {
            System.err.println("Fehler in Thread für " + myPlayer + ": " + e.getMessage());
        }
    }

    private void serveClient(Socket sock) throws IOException {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            PrintWriter out = new PrintWriter(sock.getOutputStream(), true)
        ) {
            out.println("NAME_PROMPT");

            // Hauptschleife: Kommandos lesen und verarbeiten
            while (true) {
                String json = in.readLine();
                if (json == null) {
                    System.out.println("Client beendet die Verbindung. (" + myPlayer + ")");
                    break;
                }

                GameCommand command;
                try {
                    command = GameCommandSerializer.fromJSON(json);
                } catch (Exception e) {
                    out.println("Fehler: Ungültiges JSON");
                    continue;
                }

                // Command ausführen => Events entstehen
                List<GameEvent> newEvents = processCommandAndGetEvents(command);

                // Diese neuen Events an die passenden Spieler senden
                distributeEvents(newEvents);
            }
        } finally {
            // Falls Socket-Fehler oder Schleife beendet, Thread geht zu Ende.
            // Kann man optional in playerHandlers entfernen:
            if (myPlayer != null) {
                playerHandlers.remove(myPlayer);
            }
            sock.close();
        }
    }

    /**
     * Ruft logic.processCommand(...) auf und fängt InvalidMoveException.
     * Gibt die entstandenen (oder durch Folgeschritte) erzeugten Events zurück.
     */
    private List<GameEvent> processCommandAndGetEvents(GameCommand cmd) {
        List<GameEvent> before;
        List<GameEvent> after;
        synchronized (logic) {
            // Events vor dem Ausführen
            EventCollector collector = (EventCollector) logic.getConsumer();
            before = new ArrayList<>(collector.getEvents());

            try {
                logic.processCommand(cmd);
            } catch (InvalidMoveException ime) {
                // => an den betroffenen Spieler "INVALID_MOVE" zurückschicken
                if (ime.getPlayer() != null) {
                    sendLineToPlayer(ime.getPlayer(), "INVALID_MOVE");
                    sendLineToPlayer(ime.getPlayer(), "Ungültiger Zug: " + ime.getMessage());
                    // Anschließend demselben Spieler nochmal sein PlayerTurn geben
                    reSendPlayerTurn(ime.getPlayer());
                }
                // Keine neuen Events (weil Move nicht akzeptiert)
                return Collections.emptyList();
            } catch (Exception e) {
                // Sonstige Fehler
                if (myPlayer != null) {
                    sendLineToPlayer(myPlayer, "Fehler beim Ausführen: " + e.getMessage());
                }
                return Collections.emptyList();
            }

            after = collector.getEvents();
        }
        // "newEvents" = Delta
        if (after.size() == before.size()) {
            return Collections.emptyList();
        }
        return after.subList(before.size(), after.size());
    }

    /**
     * Verteilt die entstandenen Events an die richtigen Spieler.
     */
    private void distributeEvents(List<GameEvent> events) {
        for (GameEvent ev : events) {
            if (ev instanceof PlayerRegistered pr) {
                // Hier wissen wir, welcher "Player" dieser Thread wirklich ist
                // => nur, wenn das Event zu "mir" passt
                myPlayer = pr.player();
                playerHandlers.put(myPlayer, this);

                // Sende dem Spieler sein PlayerRegistered-JSON
                sendLineToPlayer(pr.player(), GameEventSerializer.toJSON(pr));
            }
            else if (ev instanceof PlayerTurn pt) {
                // Sende an "pt.player()" alle relevanten Infos
                sendPlayerTurnInfo(pt.player());
            }
            else if (ev instanceof HandsDealt hd) {
                // Falls du jedem Spieler seine Hand teilst, könnte man hier
                // direkt das Event/Hand an "hd.player()" senden
                sendLineToPlayer(hd.player(), GameEventSerializer.toJSON(hd));
            }
            else if (ev instanceof GameOver go) {
                // An alle
                broadcast("GAME_OVER");
                broadcast(go.toString());
                // Ggf. sockets schließen
            }
            else {
                // Evtl. andere Eventtypen
                // Z.B. broadcast an alle oder an den betroffenen Player
                // ...
            }
        }
    }

    /**
     * Schickt dem Spieler p standardmäßig:
     *  - "DEINE_HAND" (+ Karten Zeile für Zeile)
     *  - "AKTUELLER_STICH" (+ Trick)
     *  - PlayerTurn-Event im JSON
     */
    private void sendPlayerTurnInfo(Player p) {
        Hand hand = logic.getState().playerHands().get(p);
        if (hand != null) {
            sendLineToPlayer(p, "DEINE_HAND");
            for (Card c : hand.cards()) {
                sendLineToPlayer(p, c.toString());
            }
        }
        // Stich
        sendLineToPlayer(p, "AKTUELLER_STICH");
        sendLineToPlayer(p, logic.getState().currentTrick().toString());

        // PlayerTurn-Event
        sendLineToPlayer(p, GameEventSerializer.toJSON(new PlayerTurn(p)));
    }

    /**
     * Bei ungültigen Zügen erneut den PlayerTurn an denselben Spieler schicken.
     */
    private void reSendPlayerTurn(Player p) {
        sendPlayerTurnInfo(p);
    }

    // -----------------------------------------------------
    // Hilfsmethoden zum Senden
    // -----------------------------------------------------

    private static synchronized RemoteClientHandler2 getHandlerFor(Player p) {
        return playerHandlers.get(p);
    }

    /** Eine Zeile Text an einen bestimmten Spieler schicken. */
    private static void sendLineToPlayer(Player p, String line) {
        RemoteClientHandler2 h = getHandlerFor(p);
        if (h == null) return;
        h.sendLine(line);
    }

    /** Broadcast an alle registrierten Spieler/Threads. */
    private static void broadcast(String line) {
        for (Player p : playerHandlers.keySet()) {
            sendLineToPlayer(p, line);
        }
    }

    /** Nicht-statisch: Schickt eine Zeile Text über *meinen* Socket */
    private void sendLine(String line) {
        try {
            if (socket.isClosed()) {
                return;
            }
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(line);
        } catch (IOException e) {
            System.err.println("Fehler beim Senden an " + myPlayer + ": " + e.getMessage());
        }
    }
}
