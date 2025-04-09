package de.hso.cardgame.ui.networkcli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import de.hso.cardgame.gamecentral.server.GameCommandSerializer;
import de.hso.cardgame.gamecentral.server.GameEventSerializer;
import de.hso.cardgame.gamecentral.server.GameLogic;
import de.hso.cardgame.model.GameCommand;
import de.hso.cardgame.model.GameEvent;

public class RemoteClientHandler extends Thread {
    private Socket socket;
    public RemoteClientHandler(Socket socket) { this.socket = socket; }
    public void run() {
        try {
            this.serveClient();
        } catch (IOException e) {
            log("terminated: " + e.getMessage());
        }
    }
    private void log(String s) {
        // Praktisch: die Logausgabe enthält den dynamischen Port des Clients.
        // Damit können wir Logmeldungen verschiedener Clients unterscheiden.
        System.out.println("[client " + this.socket.getPort() + "] " + s);
    }
    // serveClient sehr ähnlich wie vorhin, vollständiger Code
    // in den Zusatzmaterialien.
private void serveClient() throws IOException {}

	
//    private Socket socket;
//    private BufferedReader in;
//    private PrintWriter out;
//    private GameLogic logic;
//
//    public RemoteClientHandler(Socket socket, GameLogic logic) throws IOException {
//        this.socket = socket;
//        this.logic = logic;
//        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//        this.out = new PrintWriter(socket.getOutputStream(), true);
//    }
//
//    @Override
//    public void run() {
//        try {
//            String line;
//            while ((line = in.readLine()) != null) {
//                // Eingehende JSON-Daten werden in ein GameCommand umgewandelt
//                GameCommand command = GameCommandSerializer.fromJSON(line);
//                logic.processCommand(command); // Verarbeitung des Commands in der Logik
//            }
//        } catch (IOException e) {
//            System.err.println("Verbindung unterbrochen: " + e.getMessage());
//        }
//    }
//
//    public void sendEvent(GameEvent event) {
//        // GameEvent wird in JSON umgewandelt und an den Client gesendet
//        String json = GameEventSerializer.toJSON(event);
//        out.println(json);
//    }
}

