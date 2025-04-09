package de.hso.cardgame.gamecentral.server;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import de.hso.cardgame.model.Card;
import de.hso.cardgame.model.Player;
import de.hso.cardgame.model.PlayerCard;
import de.hso.cardgame.model.Rank;
import de.hso.cardgame.model.Suit;
import de.hso.cardgame.model.Trick;

import java.io.IOException;

import com.github.andrewoma.dexx.collection.List;
import com.github.andrewoma.dexx.collection.LinkedLists;


public class TrickTypeAdapter extends TypeAdapter<Trick> {

    @Override
    public void write(JsonWriter out, Trick trick) throws IOException {
    	out.beginObject();

        // Karten serialisieren
        out.name("cards");
        out.beginArray();
        for (PlayerCard playerCard : trick.cards()) {
            out.beginObject();
            out.name("player").value(playerCard.player().toString());

            // Karte serialisieren
            out.name("card");
            out.beginObject();
            out.name("suit").value(playerCard.card().suit().toString());
            out.name("rank").value(playerCard.card().rank().value());
            out.endObject();

            out.endObject();
        }
        out.endArray();

        out.endObject();
    }

    @Override
    public Trick read(JsonReader in) throws IOException {
    	List<PlayerCard> cards = LinkedLists.of(); // Dexx leere Liste erstellen

        in.beginObject();
        while (in.hasNext()) {
            String fieldName = in.nextName();
            if ("cards".equals(fieldName)) {
                in.beginArray();
                while (in.hasNext()) {
                    in.beginObject();

                    Player player = null;
                    Card card = null;

                    while (in.hasNext()) {
                        String cardField = in.nextName();
                        switch (cardField) {
                            case "player":
                                player = Player.valueOf(in.nextString());
                                break;
                            case "card":
                                in.beginObject();
                                Suit suit = null;
                                Rank rank = null;

                                while (in.hasNext()) {
                                    String cardDetail = in.nextName();
                                    switch (cardDetail) {
                                        case "suit":
                                            suit = Suit.valueOf(in.nextString());
                                            break;
                                        case "rank":
                                            rank = new Rank(in.nextInt());
                                            break;
                                    }
                                }

                                in.endObject();
                                card = new Card(suit, rank);
                                break;
                        }
                    }

                    cards = cards.append(new PlayerCard(player, card)); // Karten hinzufügen
                    in.endObject();
                }
                in.endArray();
            } else {
                in.skipValue(); // Unbekannte Felder überspringen
            }
        }
        in.endObject();

        // Erstelle einen neuen Trick mit der Kartenliste
        return new Trick(cards);
    }
}

