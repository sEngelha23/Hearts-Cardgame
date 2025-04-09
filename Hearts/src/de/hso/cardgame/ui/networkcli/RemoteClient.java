package de.hso.cardgame.ui.networkcli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import de.hso.cardgame.gamecentral.server.GameCommandSerializer;
import de.hso.cardgame.gamecentral.server.GameEventSerializer;
import de.hso.cardgame.model.GameCommand;
import de.hso.cardgame.model.GameEvent;
import de.hso.cardgame.ui.Client;

public class RemoteClient extends Client {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public RemoteClient(String host, int port) throws IOException {
        this.socket = new Socket(host, port);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
    }

    @Override
    public void register(String name) {
        sendCommand(new GameCommand.RegisterPlayer(name));
    }

    @Override
    public GameEvent readGameEvent() {
        try {
            String json = in.readLine();
            return GameEventSerializer.fromJSON(json);
        } catch (IOException e) {
            throw new RuntimeException("Fehler beim Lesen des GameEvents", e);
        }
    }

    public void sendCommand(GameCommand command) {
        String json = GameCommandSerializer.toJSON(command);
        out.println(json);
    }
}

