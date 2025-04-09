package de.hso.cardgame.gamecentral.server;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;


import de.hso.cardgame.model.GameEvent;
import de.hso.cardgame.model.Hand;
import de.hso.cardgame.model.Player;


import java.io.IOException;

public class HandsDealtAdapter extends TypeAdapter<GameEvent.HandsDealt> {

    private final Gson gson;

    public HandsDealtAdapter(Gson gson) {
        this.gson = gson;
    }

    @Override
    public void write(JsonWriter out, GameEvent.HandsDealt handsDealt) throws IOException {
        out.beginObject();
        out.name("type").value("HandsDealt");
        out.name("player").value(handsDealt.player().toString());

        // Schreibe die Hand
        out.name("hand");
        gson.getAdapter(Hand.class).write(out, handsDealt.hand());

        out.endObject();
    }

    @Override
    public GameEvent.HandsDealt read(JsonReader in) throws IOException {
        Player player = null;
        Hand hand = null;

        in.beginObject();
        while (in.hasNext()) {
            String name = in.nextName();
            switch (name) {
                case "player" -> {
                    player = Player.valueOf(in.nextString());
                }
                case "hand" -> {
                    HandAdapter handAdapter = new HandAdapter(gson);
                    hand = handAdapter.read(in);

                }
                default -> in.skipValue();
            }
        }
        in.endObject();

        if (player == null || hand == null) {
            throw new JsonParseException("Missing required fields in HandsDealt JSON");
        }

        GameEvent.HandsDealt result = new GameEvent.HandsDealt(player, hand);
        return result;
    }
}


