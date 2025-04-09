package de.hso.cardgame.gamecentral.server; 
import com.github.andrewoma.dexx.collection.*;
import com.google.gson.*;

import de.hso.cardgame.model.Hand;
import de.hso.cardgame.model.Score;
import de.hso.cardgame.model.*;
import de.hso.cardgame.model.GameEvent.PlayerRegistered;

import java.lang.reflect.Type;
import java.util.stream.Collectors;

import com.google.gson.reflect.*;
import com.google.gson.stream.JsonReader;
import java.io.StringReader;
import java.lang.Iterable;
import de.hso.cardgame.gamecentral.server.TrickSerializer;


public class GameEventSerializer {
    public static String toJSON(GameEvent event) {
        var result = new JsonObject();

        if (event instanceof GameEvent.PlayerRegistered) {
                GameEvent.PlayerRegistered playerRegistered = (GameEvent.PlayerRegistered) event;

                result.addProperty("type", "PlayerRegistered");
                result.addProperty("player", playerRegistered.player().toString());
                result.addProperty("playerName", playerRegistered.name());

                var otherPlayersJson = new JsonObject();
                for (com.github.andrewoma.dexx.collection.Pair<Player, String> entry : playerRegistered.otherPlayers()) {
                    otherPlayersJson.addProperty(entry.component1().toString(), entry.component2());
                }
                result.add("otherPlayers", otherPlayersJson);
            

        } else if (event instanceof GameEvent.HandsDealt) {
            GameEvent.HandsDealt handsDealt = (GameEvent.HandsDealt) event;

            result.addProperty("type", "HandsDealt");
            result.addProperty("player", handsDealt.player().toString());

            JsonObject handJson = new JsonObject();
            JsonArray cardsArray = new JsonArray();

            for (Card card : handsDealt.hand().cards()) {
                JsonObject cardJson = new JsonObject();
                cardJson.addProperty("suit", card.suit().toString());
                cardJson.addProperty("rank", card.rank().value());
                cardsArray.add(cardJson);
            }

            handJson.add("cards", cardsArray);
            result.add("hand", handJson);
        } else if (event instanceof GameEvent.PlayerTurn) {
        	GameEvent.PlayerTurn playerTurn = (GameEvent.PlayerTurn) event;
            result.addProperty("type", "PlayerTurn");
            result.addProperty("player", playerTurn.player().toString());
            
            
        } else if (event instanceof GameEvent.CardPlayed) {
        	if (event instanceof GameEvent.CardPlayed) {
        	    GameEvent.CardPlayed cardPlayed = (GameEvent.CardPlayed) event;

        	    result.addProperty("type", "CardPlayed");
        	    result.addProperty("player", cardPlayed.player().toString());

        	    JsonObject cardJson = new JsonObject();
        	    cardJson.addProperty("suit", cardPlayed.card().suit().toString());
        	    cardJson.addProperty("rank", cardPlayed.card().rank().value()); // Nur den numerischen Wert speichern
        	    result.add("card", cardJson);
        	}

            
            
        } else if (event instanceof GameEvent.TrickTaken) {
        	GameEvent.TrickTaken trickTaken = (GameEvent.TrickTaken) event;
            result.addProperty("type", "TrickTaken");
            result.addProperty("player", trickTaken.player().toString());
            Gson gsonblub = new GsonBuilder()
            	    .registerTypeAdapter(Trick.class, new TrickSerializer())
            	    .registerTypeAdapter(PlayerCard.class, new TrickAdapter())
            	    .create();
            JsonElement trickJson = gsonblub.toJsonTree(trickTaken.trick());
           	result.add("trick", trickJson);
            
            
        } else if (event instanceof GameEvent.GameError) {
        	GameEvent.GameError gameError = (GameEvent.GameError) event;
            result.addProperty("type", "GameError");
            result.addProperty("msg", gameError.msg());
            
            
        } else if (event instanceof GameEvent.PlayerError) {
        	GameEvent.PlayerError playerError = (GameEvent.PlayerError) event;
            result.addProperty("type", "PlayerError");
            result.addProperty("player", playerError.player().toString());
            result.addProperty("msg", playerError.msg());
            
            
        } else if (event instanceof GameEvent.GameOver) {
            GameEvent.GameOver gameOver = (GameEvent.GameOver) event;
            result.addProperty("type", "GameOver");
            JsonObject scoreJson = new JsonObject();
            for (Pair<Player, Integer> entry : gameOver.score().score()) {
                scoreJson.addProperty(entry.component1().toString(), entry.component2());
            }

            result.add("score", scoreJson);
        }

        return result.toString();
    }
    
    public static GameEvent fromJSON(String json) {
        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
        String eventType = jsonObject.get("type").getAsString();

        Gson gson = new GsonBuilder()
            .registerTypeAdapter(GameEvent.PlayerRegistered.class, new PlayerRegisteredAdapter())
            
            .registerTypeAdapter(new TypeToken<Set<Card>>() {}.getType(), new DexxSetAdapter<>(new Gson()))
            .registerTypeAdapter(Hand.class, new HandAdapter(new Gson()))
            .registerTypeAdapter(GameEvent.HandsDealt.class, new HandsDealtAdapter(new Gson()))
            .registerTypeAdapter(Trick.class, new TrickTypeAdapter())
            .registerTypeAdapter(Score.class, new ScoreAdapter())
            .registerTypeAdapter(List.class, new DexxListAdapter<>())
            .registerTypeAdapter(List.class, new DexxListInstanceCreator<>())
            .registerTypeAdapter(PlayerCard.class, new TrickAdapter())
            .registerTypeAdapter(GameEvent.GameOver.class, new GameOverTypeAdapter(new Gson())) // Registriere GameOverAdapter
            .setPrettyPrinting()
            .create();

        switch (eventType) {
            case "PlayerRegistered":
                return gson.fromJson(json, GameEvent.PlayerRegistered.class);

            case "HandsDealt":
                GameEvent.HandsDealt handsDealt = gson.fromJson(json, GameEvent.HandsDealt.class);
                return handsDealt;    

            case "PlayerTurn": {
                String playerString = jsonObject.get("player").getAsString();
                Player player = Player.valueOf(playerString);
                return new GameEvent.PlayerTurn(player);
            }

            case "CardPlayed": {
                String playerString = jsonObject.get("player").getAsString();
                Player player = Player.valueOf(playerString);

                JsonObject cardJson = jsonObject.getAsJsonObject("card");
                Suit suit = Suit.valueOf(cardJson.get("suit").getAsString());
                Rank rank = new Rank(cardJson.get("rank").getAsInt());
                Card card = new Card(suit, rank);

                return new GameEvent.CardPlayed(player, card);
            }

            case "TrickTaken":
                // Spieler verarbeiten
                String playerString2 = jsonObject.get("player").getAsString();
                Player player2 = Player.valueOf(playerString2);

                // Trick verarbeiten
                JsonObject trickJson = jsonObject.getAsJsonObject("trick");
                Trick trick = gson.fromJson(trickJson, Trick.class);

                // GameEvent erstellen
                return new GameEvent.TrickTaken(player2, trick);

            case "GameError": {
                String msg = jsonObject.get("msg").getAsString();
                return new GameEvent.GameError(msg);
            }

            case "PlayerError": {
                String playerString = jsonObject.get("player").getAsString();
                Player player = Player.valueOf(playerString);
                String msg = jsonObject.get("msg").getAsString();
                return new GameEvent.PlayerError(player, msg);
            }

            case "GameOver":            	
            	JsonObject scoreObject = jsonObject.getAsJsonObject("score");

                // Map<Player, Integer> aus dem JSON extrahieren
                Map<Player, Integer> scoreMap = Maps.of(); // Leere Dexx-Map
                for (String key : scoreObject.keySet()) {
                    Player player = Player.valueOf(key); // Schlüssel als Player
                    Integer value = scoreObject.get(key).getAsInt(); // Wert als Integer
                    scoreMap = scoreMap.put(player, value); // Füge zur Dexx-Map hinzu
                    
                }
                Score score = new Score(scoreMap);
                return new GameEvent.GameOver(score);

                // GameOver-Event mit der Map erstellen
                

            default:
                throw new IllegalArgumentException("Unknown event type: " + eventType);
        }
    }

    
    @SuppressWarnings("static-access")
	public static void main(String[] args) {
    	/// Player Registered
    	GameEventSerializer ges = new GameEventSerializer();

    	Player player = Player.P1;
    	Map<Player, String> otherPlayers = Maps.of(Player.P2, "Player2").put(Player.P3, "Player3");
    	GameEvent.PlayerRegistered playerRegistered = new GameEvent.PlayerRegistered(player, "Player1", otherPlayers);

    	String json = ges.toJSON(playerRegistered);
    	System.out.println(json);

    	GameEvent deserialized = ges.fromJSON(json);
    	System.out.println(deserialized);

        
        /// Player Turn
    	GameEvent.PlayerTurn pturn = new GameEvent.PlayerTurn(Player.P1);
    	
    	System.out.println(ges.toJSON(pturn));
    	String pTurn = ges.toJSON(pturn);
    	GameEvent pTurnfrom = ges.fromJSON(pTurn);
        System.out.println(pTurnfrom);
    	///
//    	
//        /// TrickTaken
    	Card hearts8 = new Card(Suit.Hearts, new Rank(8));
    	Card hearts10 = new Card(Suit.Hearts, new Rank(10));
    	Trick trick = Trick.empty;
    	trick = trick.addCard(Player.P1, hearts8);
    	trick = trick.addCard(Player.P2, hearts10);
        GameEvent.TrickTaken tTaken = new GameEvent.TrickTaken(Player.P1, trick);

        System.out.println(ges.toJSON(tTaken));
        
    	String tTakenFrom = ges.toJSON(tTaken);
        GameEvent tTakenEvent = ges.fromJSON(tTakenFrom);
    	System.out.println(tTakenEvent);
//        
    	/// CardPlayed
        GameEvent.CardPlayed cPlayed = new GameEvent.CardPlayed(Player.P1, hearts10);
        System.out.println(ges.toJSON(cPlayed));
        String cPlayedFrom = ges.toJSON(cPlayed);
        GameEvent neucPlayed = ges.fromJSON(cPlayedFrom);
        System.out.println(neucPlayed);
        
        // 
//        
//        
//        /// HandsDealt
        
        Player player23 = Player.P1;
        Set<Card> cards = Sets.of(new Card(Suit.Hearts, new Rank(10)))
            .add(new Card(Suit.Hearts, new Rank(8)));
        Hand hand4 = new Hand(cards);
        GameEvent.HandsDealt handsDealt = new GameEvent.HandsDealt(player23, hand4);

        String json232323 = ges.toJSON(handsDealt);
        System.out.println(json232323);

        // JSON deserialisieren
        try {
            GameEvent deserialized2323 = ges.fromJSON(json232323);
            System.out.println(deserialized2323);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        
//        
//        /// GAMEERROR FUNKTIONIERT ALLES
//        
        /// PlayerError
        GameEvent.PlayerError pError = new GameEvent.PlayerError(Player.P3, "FehlerSpieler3");
        System.out.println(ges.toJSON(pError));
        System.out.println(ges.fromJSON(ges.toJSON(pError)));
        
//        ///
//        
        ///GameOver
        Map<Player, Integer> exampleMap = Maps.of(
                Player.P1, 10,
                Player.P2, 20,
                Player.P3, 15,
                Player.P4, 30
            );

            Score score = new Score(exampleMap);

        
        GameEvent gOver = new GameEvent.GameOver(score);
        System.out.println(ges.toJSON(gOver));
        System.out.println(ges.fromJSON(ges.toJSON(gOver)));

    
        }


}


