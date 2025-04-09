package de.hso.cardgame.gamecentral.server;

import com.github.andrewoma.dexx.collection.Map;
import com.github.andrewoma.dexx.collection.Maps;
import com.github.andrewoma.dexx.collection.Pair;
import com.google.gson.*;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class DexxMapAdapter<K, V> implements JsonSerializer<Map<K, V>>, JsonDeserializer<Map<K, V>> {

    @Override
    public JsonElement serialize(Map<K, V> src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();

        // Iteriere über die Dexx-Map mittels Pair
        for (Pair<K, V> pair : src) {
            String key = context.serialize(pair.component1()).getAsString();
            JsonElement value = context.serialize(pair.component2());
            jsonObject.add(key, value);
        }

        return jsonObject;
    }

    @Override
    public Map<K, V> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Map<K, V> map = Maps.of(); // Leere Dexx-Map
        JsonObject jsonObject = json.getAsJsonObject();

        // Generische Typen aus typeOfT extrahieren
        Type keyType = ((ParameterizedType) typeOfT).getActualTypeArguments()[0];
        Type valueType = ((ParameterizedType) typeOfT).getActualTypeArguments()[1];

        // Iteriere über die JSON-Schlüssel und baue die Dexx-Map
        for (String key : jsonObject.keySet()) {
            K k = context.deserialize(new JsonPrimitive(key), keyType);
            V v = context.deserialize(jsonObject.get(key), valueType);
            map = map.put(k, v);
        }

        return map;
    }
}

