package de.hso.cardgame.ui.networkcli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class GameServer {
	private static final int PORT = 12345;

	public static void main(String[] args) {
		System.out.println("Server startet. Warte auf Spieler...");

		try (ServerSocket serverSocket = new ServerSocket(PORT)) {
			while (true) {
				Socket playerSocket = serverSocket.accept();
				System.out.println("Ein Spieler hat sich verbunden: " + playerSocket.getInetAddress());

// Testnachricht an den Client senden
				PrintWriter out = new PrintWriter(playerSocket.getOutputStream(), true);
				out.println("Willkommen auf dem GameServer!");

// Client-Verbindung offen halten
				new Thread(() -> handleClient(playerSocket)).start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void handleClient(Socket socket) {
		try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

// Begrüßungsnachricht direkt senden
			out.println("Willkommen! Bitte gib deinen Namen ein:");

// Namen vom Client lesen
			String name = in.readLine();

			if (name != null && !name.isEmpty()) {
				System.out.println("Spieler registriert: " + name);
				out.println("Danke, " + name + "! Verbindung bleibt aktiv...");
			} else {
				System.out.println("Kein gültiger Name vom Client erhalten.");
				out.println("Fehler: Kein gültiger Name erhalten.");
			}

		} catch (IOException e) {
			System.err.println("Fehler bei der Verarbeitung des Clients: " + e.getMessage());
		}
	}


}

//package de.hso.cardgame.ui.networkcli;
//import java.io.IOException;
//import java.net.ServerSocket;
//import java.net.Socket;
//	
//public class GameServer {
//
//	public static void main(String[] args) throws IOException {
//	    int port = 12345;
//	    try (ServerSocket serverSocket = new ServerSocket(port)) {
//	        System.out.println("Waiting for clients on port " + port);
//	        while (true) {
//	            Socket sock = serverSocket.accept();
//	            System.out.println("Got new connection: " + sock);
//	            RemoteClientHandler h = new RemoteClientHandler(sock);
//	            h.start();
//	        }
//	    }
//	}
//
//}
