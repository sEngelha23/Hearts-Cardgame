package de.hso.cardgame.gamecentral.server;

import com.github.andrewoma.dexx.collection.List;
import com.github.andrewoma.dexx.collection.LinkedLists;
import com.google.gson.*;

import java.lang.reflect.Type;

public class DexxListAdapter<T> implements JsonDeserializer<List<T>>, JsonSerializer<List<T>> {

    @Override
    public JsonElement serialize(List<T> src, Type typeOfSrc, JsonSerializationContext context) {
        JsonArray jsonArray = new JsonArray();
        for (T item : src) {
            jsonArray.add(context.serialize(item));
        }
        return jsonArray;
    }

    @Override
    public List<T> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (!json.isJsonArray()) {
            throw new JsonParseException("Expected a JSON array");
        }

        JsonArray jsonArray = json.getAsJsonArray();
        List<T> list = LinkedLists.of(); // Start mit einer leeren Liste
        for (JsonElement element : jsonArray) {
            T item = context.deserialize(element, ((Class<T>) ((java.lang.reflect.ParameterizedType) typeOfT).getActualTypeArguments()[0]));
            list = list.append(item);
        }
        return list;
    }
}
