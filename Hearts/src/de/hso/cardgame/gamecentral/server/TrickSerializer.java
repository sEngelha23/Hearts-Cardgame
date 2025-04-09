package de.hso.cardgame.gamecentral.server;

import java.lang.reflect.Type;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import de.hso.cardgame.model.PlayerCard;
import de.hso.cardgame.model.Trick;


public class TrickSerializer implements JsonSerializer<Trick> {
    
	@Override
	public JsonElement serialize(Trick trick, Type typeOfSrc, JsonSerializationContext context) {
		JsonObject jsonObject = new JsonObject();

        // Konvertiere die Karten in ein JSON-Array
        JsonArray cardsArray = new JsonArray();
        for (PlayerCard card : trick.cards()) {
            JsonElement cardJson = context.serialize(card, PlayerCard.class);
            cardsArray.add(cardJson);
        }

        jsonObject.add("cards", cardsArray);
        return jsonObject;
	}
}
