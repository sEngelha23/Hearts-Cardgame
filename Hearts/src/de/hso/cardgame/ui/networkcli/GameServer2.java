package de.hso.cardgame.ui.networkcli;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import de.hso.cardgame.gamecentral.server.EventCollector;
import de.hso.cardgame.gamecentral.server.GameLogic;
import de.hso.cardgame.gamecentral.server.GameState;

public class GameServer2 {
    private static final int PORT = 127; 
    private static final GameState initialState = GameState.empty;
    private static final EventCollector eventCollector = new EventCollector();
    private static final GameLogic logic = new GameLogic(initialState, eventCollector);

    public static void main(String[] args) {
        System.out.println("Server startet auf Port " + PORT + ". Warte auf Spieler...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket playerSocket = serverSocket.accept();
                System.out.println("Neuer Client verbunden: " + playerSocket.getInetAddress());

                // Optional kleine Begrüßung
                PrintWriter out = new PrintWriter(playerSocket.getOutputStream(), true);
                out.println("Verbunden mit dem GameServer!");
                
                // Pro Verbindung ein neuer Thread
                RemoteClientHandler2 handler = new RemoteClientHandler2(playerSocket, logic);
                handler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
