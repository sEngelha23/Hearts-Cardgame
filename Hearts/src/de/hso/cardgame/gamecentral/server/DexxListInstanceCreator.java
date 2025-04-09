package de.hso.cardgame.gamecentral.server;

import com.github.andrewoma.dexx.collection.List;
import com.github.andrewoma.dexx.collection.LinkedLists;
import com.google.gson.InstanceCreator;

import java.lang.reflect.Type;

public class DexxListInstanceCreator<T> implements InstanceCreator<List<T>> {
    @Override
    public List<T> createInstance(Type type) {
        // Rückgabe einer leeren veränderbaren Dexx-Liste
        return LinkedLists.of();
    }
}
