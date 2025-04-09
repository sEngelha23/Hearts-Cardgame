package de.hso.cardgame.gamecentral.server;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import de.hso.cardgame.model.GameEvent;
import de.hso.cardgame.model.Score;

import java.io.IOException;

public class GameOverTypeAdapter extends TypeAdapter<GameEvent.GameOver> {

    private final Gson gson;

    public GameOverTypeAdapter(Gson gson) {
        this.gson = gson;
    }

    @Override
    public void write(JsonWriter out, GameEvent.GameOver gameOver) throws IOException {
        out.beginObject();
        out.name("type").value("GameOver");
        out.name("score");
        gson.getAdapter(Score.class).write(out, gameOver.score());
        out.endObject();
    }

    @Override
    public GameEvent.GameOver read(JsonReader in) throws IOException {
        in.beginObject();
        Score score = null;

        while (in.hasNext()) {
            String name = in.nextName();
            if ("score".equals(name)) {
                score = gson.getAdapter(Score.class).read(in);
            } else {
                in.skipValue();
            }
        }
        in.endObject();

        if (score == null) {
            throw new JsonParseException("Missing 'score' field in GameOver JSON");
        }

        return new GameEvent.GameOver(score);
    }
}
