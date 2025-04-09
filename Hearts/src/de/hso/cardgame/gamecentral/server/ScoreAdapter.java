package de.hso.cardgame.gamecentral.server;

import com.github.andrewoma.dexx.collection.Map;
import com.github.andrewoma.dexx.collection.Maps;
import com.github.andrewoma.dexx.collection.Pair;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import de.hso.cardgame.model.Player;
import de.hso.cardgame.model.Score;

import java.io.IOException;



public class ScoreAdapter extends TypeAdapter<Score> {

    @Override
    public void write(JsonWriter out, Score score) throws IOException {
        out.beginObject();

        // Iteriere über alle Schlüssel
        for (Player key : score.score().keys()) {
            Integer value = score.score().get(key); // Hole den Wert für den Schlüssel
            out.name(key.toString()).value(value); // Schreibe den Schlüssel-Wert-Paar
        }

        out.endObject();
    }

    @Override
    public Score read(JsonReader in) throws IOException {
        Map<Player, Integer> map = Maps.of(); // Leere Dexx-Map

        in.beginObject();
        while (in.hasNext()) {
            String key = in.nextName(); // Lies den Schlüssel als String
            Player player = Player.valueOf(key); // Konvertiere den Schlüssel in Player
            Integer value = in.nextInt(); // Lies den Wert
            map = map.put(player, value); // Füge das Paar zur Map hinzu
        }
        in.endObject();

        return new Score(map); // Erstelle ein neues Score-Objekt
    }
}
