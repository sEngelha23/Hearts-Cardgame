package de.hso.cardgame.gamecentral.server;

import com.github.andrewoma.dexx.collection.Set;
import com.github.andrewoma.dexx.collection.Sets;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class DexxSetAdapter<E> extends TypeAdapter<com.github.andrewoma.dexx.collection.Set<E>> {

    private final Gson gson;

    public DexxSetAdapter(Gson gson) {
        this.gson = gson;
    }

    @Override
    public void write(JsonWriter out, com.github.andrewoma.dexx.collection.Set<E> set) throws IOException {
        out.beginArray();
        for (E element : set) {
            gson.toJson(element, element.getClass(), out);
        }
        out.endArray();
    }

    @Override
    public com.github.andrewoma.dexx.collection.Set<E> read(JsonReader in) throws IOException {
        com.github.andrewoma.dexx.collection.Set<E> set = com.github.andrewoma.dexx.collection.Sets.of();
        in.beginArray();
        while (in.hasNext()) {
            E element = gson.fromJson(in, new TypeToken<E>() {}.getType());
            set = set.add(element);
        }
        in.endArray();
        return set;
    }
}


