package de.hso.cardgame.gamecentral.server;

import com.github.andrewoma.dexx.collection.Set;
import com.github.andrewoma.dexx.collection.Sets;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import de.hso.cardgame.model.Card;
import de.hso.cardgame.model.Hand;

import java.io.IOException;

public class HandAdapter extends TypeAdapter<Hand> {

    private final Gson gson;

    public HandAdapter(Gson gson) {
        this.gson = gson;
    }

    @Override
    public void write(JsonWriter out, Hand hand) throws IOException {
        out.beginObject();
        out.name("cards");
        out.beginArray();

        // Schreibe alle Karten in das JSON-Array
        for (Card card : hand.cards()) {
            gson.getAdapter(Card.class).write(out, card);
        }

        out.endArray();
        out.endObject();
    }

    @Override
    public Hand read(JsonReader in) throws IOException {
        DexxSetAdapter<Card> setAdapter = new DexxSetAdapter<>(gson); // Verwende den DexxSetAdapter direkt
        com.github.andrewoma.dexx.collection.Set<Card> cardSet = Sets.of();
        in.beginObject();
        while (in.hasNext()) {
            String name = in.nextName();
            if ("cards".equals(name)) {
                cardSet = setAdapter.read(in); // Verwendet DexxSetAdapter
            } else {
                in.skipValue();
            }
        }
        in.endObject();
        return new Hand(cardSet);
    }


}

