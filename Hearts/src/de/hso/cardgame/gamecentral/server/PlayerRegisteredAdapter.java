package de.hso.cardgame.gamecentral.server;

import com.github.andrewoma.dexx.collection.Map;
import com.github.andrewoma.dexx.collection.Maps;
import com.github.andrewoma.dexx.collection.Pair;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import de.hso.cardgame.model.GameEvent;
import de.hso.cardgame.model.GameEvent.PlayerRegistered;
import de.hso.cardgame.model.Player;

import java.io.IOException;

public class PlayerRegisteredAdapter extends TypeAdapter<GameEvent.PlayerRegistered> {

    @Override
    public void write(JsonWriter out, GameEvent.PlayerRegistered playerRegistered) throws IOException {
        out.beginObject();
        out.name("type").value("PlayerRegistered");
        out.name("player").value(playerRegistered.player().toString());
        out.name("playerName").value(playerRegistered.name());

        // Konvertiere die Dexx-Map in ein JSON-Objekt
        JsonObject otherPlayersJson = new JsonObject();
        for (Pair<Player, String> pair : playerRegistered.otherPlayers()) {
            otherPlayersJson.addProperty(pair.component1().toString(), pair.component2());
        }
        out.name("otherPlayers").jsonValue(otherPlayersJson.toString());
        out.endObject();
    }

    @Override
    public GameEvent.PlayerRegistered read(JsonReader in) throws IOException {
        Player player = null;
        String name = null;
        Map<Player, String> otherPlayers = Maps.of(); // Leere Dexx-Map

        in.beginObject(); // Beginnt das JSON-Objekt zu lesen
        while (in.hasNext()) {
            String fieldName = in.nextName();
            switch (fieldName) {
                case "player":
                    player = Player.valueOf(in.nextString());
                    break;
                case "playerName":
                    name = in.nextString();
                    break;
                case "otherPlayers":
                    in.beginObject(); // Beginne die Verarbeitung des JSON-Objekts für otherPlayers
                    while (in.hasNext()) {
                        String key = in.nextName(); // Liest den Player als Schlüssel
                        String value = in.nextString(); // Liest den Wert
                        otherPlayers = otherPlayers.put(Player.valueOf(key), value); // Fügt das Paar zur Dexx-Map hinzu
                    }
                    in.endObject(); // Beende das JSON-Objekt für otherPlayers
                    break;
                default:
                    in.skipValue(); // Überspringt unbekannte Felder
                    break;
            }
        }
        in.endObject(); // Beendet das Lesen des JSON-Objekts

        return new GameEvent.PlayerRegistered(player, name, otherPlayers);
    }



}

