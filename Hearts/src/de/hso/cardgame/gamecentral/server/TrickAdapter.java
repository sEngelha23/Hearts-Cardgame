package de.hso.cardgame.gamecentral.server;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import de.hso.cardgame.model.Card;
import de.hso.cardgame.model.Player;
import de.hso.cardgame.model.PlayerCard;
import de.hso.cardgame.model.Rank;
import de.hso.cardgame.model.Suit;

import java.io.IOException;

public class TrickAdapter extends TypeAdapter<PlayerCard> {

   


	@Override
	public PlayerCard read(JsonReader in) throws IOException {

        in.beginObject();
        Player player = null;
        Card card = null;

        while (in.hasNext()) {
            String name = in.nextName();
            switch (name) {
                case "player" -> player = Player.valueOf(in.nextString());
                case "card" -> {
                    in.beginObject();
                    Suit suit = null;
                    Rank rank = null;
                    while (in.hasNext()) {
                        String field = in.nextName();
                        switch (field) {
                            case "suit" -> suit = Suit.valueOf(in.nextString());
                            case "rank" -> rank = new Rank(in.nextInt());
                        }
                    }
                    in.endObject();
                    card = new Card(suit, rank);
                }
            }
        }
        in.endObject();

        return new PlayerCard(player, card); // Konstruktor wird verwendet
	}

	@Override
	public void write(JsonWriter out, PlayerCard playerCard) throws IOException {
		out.beginObject();
        out.name("player").value(playerCard.player().toString());
        out.name("card");
        out.beginObject();
        out.name("suit").value(playerCard.card().suit().toString());
        out.name("rank").value(playerCard.card().rank().value());
        out.endObject();
        out.endObject();
		
	}
}
