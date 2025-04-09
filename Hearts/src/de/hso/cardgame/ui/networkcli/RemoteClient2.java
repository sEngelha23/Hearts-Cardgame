package de.hso.cardgame.ui.networkcli;

import de.hso.cardgame.gamecentral.server.GameCommandSerializer;
import de.hso.cardgame.gamecentral.server.GameEventSerializer;
import de.hso.cardgame.model.GameCommand;
import de.hso.cardgame.model.GameEvent;
import de.hso.cardgame.ui.Client;
import java.io.*;
import java.net.Socket;
import java.util.Scanner;


public class RemoteClient2 extends Client {

    private final Socket socket;
    private final BufferedReader in;
    private final PrintWriter out;

    /**
     * Konstruktor: Baut die Verbindung zum Server auf und
     * initialisiert die Ein-/Ausgabeströme.
     */
    public RemoteClient2(String host, int port) throws IOException {
        // Socket-Verbindung aufbauen
        this.socket = new Socket("localhost", port);

        // Eingehende Nachrichten (vom Server) als Text
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        // Ausgehende Nachrichten (zum Server) als Text
        this.out = new PrintWriter(socket.getOutputStream(), true);

        System.out.println("Client: Verbindung zu " + host + ":" + port + " hergestellt.");
    }

    /**
     * Registriert den Spieler durch Senden eines RegisterPlayer-Kommandos.
     * Im Minimalfall ist das alles an "Logik", was im Client notwendig ist.
     */
    @Override
    public void register(String name) {
        GameCommand cmd = new GameCommand.RegisterPlayer(name);
        String json = GameCommandSerializer.toJSON(cmd);
        out.println(json);  // Senden an den Server
    }

    /**
     * Liest blockierend das nächste GameEvent in JSON-Form vom Server.
     * Gibt null zurück, wenn die Verbindung unterbrochen ist.
     */
    @Override
    public GameEvent readGameEvent() {
        try {
            String line = in.readLine();
            if (line == null) {
                // null bedeutet: der Server hat die Verbindung geschlossen
                return null;
            }
            // JSON in ein GameEvent umwandeln
            return GameEventSerializer.fromJSON(line);
        } catch (IOException e) {
            System.err.println("Fehler beim Lesen eines GameEvents: " + e.getMessage());
            return null;
        }
    }
    
    public static void main(String[] args) {
        final String SERVER = "localhost"; // IP-Adresse oder Hostname des Servers
        final int PORT = 230; // Port des Servers

        try (Socket socket = new Socket(SERVER, PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("Verbindung zum Server hergestellt.");

            // Begrüßungsnachricht vom Server empfangen
            String serverMessage = in.readLine();
            System.out.println("Server: " + serverMessage);

            // Spielernamen eingeben
            String test = in.readLine();
            System.out.println(test);
            String playerName = scanner.nextLine();

            // Namen an den Server senden
            out.println(playerName);

            // Bestätigung vom Server empfangen
            String confirmation = in.readLine();
            System.out.println("Server: " + confirmation);

            // Starte die Hauptspielschleife (falls erforderlich)
            RemoteClient client = new RemoteClient(SERVER, PORT);
            client.run();

        } catch (IOException e) {
            System.err.println("Fehler beim Verbinden: " + e.getMessage());
        }
    }

    /**
     * Die abstrakte Klasse Client ruft am Ende von run() diese Methode auf.
     * Hier können wir den Socket und Ressourcen schließen.
     */

}
//public class RemoteClient2 extends Client {
//
//	public static void main(String[] args) throws IOException {
//		if (args.length != 2) {
//			System.out.println("USAGE: java DateTimeClient HOST PORT");
//			System.exit(1);
//		}
//		String host = args[0];
//		int port = Integer.parseInt(args[1]);
//		System.out.println("Connecting to " + host + ":" + port);
//		try (Socket sock = new Socket(host, port)) {
//			Writer w = new OutputStreamWriter(sock.getOutputStream());
//			
//			BufferedReader r = new BufferedReader(new InputStreamReader(sock.getInputStream()));
//			
//			var in = new Scanner(System.in);
//			 
//			in.useDelimiter("\n"); // Lese zeilenweise von stdin
//			
//			while (in.hasNext()) { // Solange der Benutzer etwas eingibt,
//				String cmd = in.next(); // lese die nächste Eingabe.
//				
//				
//				w.append(cmd + "\n"); // Sende die Eingabe an den Server
//				w.flush();
//				String fromServer = r.readLine(); // Lese das Ergebnis vom Server
//				if (fromServer == null) {
//					break;
//				}
//				System.out.println(fromServer);
//			}
//		}
//	}
//
//	@Override
//	public void register(String name) {
//		// TODO Auto-generated method stub
//
//	}
//
//	@Override
//	public GameEvent readGameEvent() {
//		GameEvent event = GameEventSerializer.fromJSON(null);
//		return null;
//	}
//	
//	public String sendCommand(GameCommand cmd) {
//		String json = GameCommandSerializer.toJSON(cmd);
//		return json;
//	}
//	
//}



//
//
//import de.hso.cardgame.model.GameCommand;
//import de.hso.cardgame.model.GameEvent;
//import de.hso.cardgame.gamecentral.server.GameCommandSerializer;
//import de.hso.cardgame.gamecentral.server.GameEventSerializer;
//import de.hso.cardgame.ui.Client;
//import de.hso.cardgame.ui.UI;
//
//import java.io.*;
//import java.net.Socket;
//
///**
// * Ein Client, der über ein Netzwerk-Socket mit dem CardGameServer kommuniziert.
// */
//public class RemoteClient2 extends Client {
//    private Socket socket;
//    private BufferedReader in;    // Zum Lesen vom Server
//    private PrintWriter out;      // Zum Schreiben an den Server
//
//    public RemoteClient2(String host, int port) throws IOException {
//        // Stelle die Verbindung her
//        this.socket = new Socket(host, port);
//        // Text-basiertes Ein-/Ausgabesystem
//        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//        this.out = new PrintWriter(socket.getOutputStream(), true);
//    }
//
//    /**
//     * Schickt einen RegisterPlayer-Befehl als JSON an den Server.
//     */
//    @Override
//    public void register(String name) {
//        GameCommand.RegisterPlayer cmd = new GameCommand.RegisterPlayer(name);
//        sendCommand(cmd);
//    }
//
//    /**
//     * Liest blockierend eine JSON-Zeile vom Server und wandelt sie in ein GameEvent um.
//     * Gibt null zurück, wenn die Verbindung unterbrochen ist.
//     */
//    @Override
//    public GameEvent readGameEvent() {
//        try {
//            String line = in.readLine();  // Wartet auf nächste Zeile
//            if (line == null) {
//                // null bedeutet: Server hat die Verbindung geschlossen
//                return null;
//            }
//            return GameEventSerializer.fromJSON(line);
//        } catch (IOException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
//
//    /**
//     * Verschickt einen GameCommand an den Server, codiert als JSON-Zeile.
//     */
//    public void sendCommand(GameCommand cmd) {
//        String json = GameCommandSerializer.toJSON(cmd);
//        out.println(json);  // PrintWriter, also automatische Zeilenumbruch
//        out.flush();        // sicherstellen, dass gesendet wird
//    }
//
//    /**
//     * Schließt den Socket sauber, wenn das Spiel zu Ende ist.
//     */
////    @Override
////    public void dispose() {
////        try {
////            socket.close();
////        } catch (IOException e) {
////            // Ignorieren oder loggen
////        }
////    }
//}
