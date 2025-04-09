package de.hso.cardgame.gamecentral.server;

import com.github.andrewoma.dexx.collection.HashSet;
import com.github.andrewoma.dexx.collection.Map;
import com.github.andrewoma.dexx.collection.Maps;
import com.github.andrewoma.dexx.collection.Set;
import com.github.andrewoma.dexx.collection.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import de.hso.cardgame.model.Card;
import de.hso.cardgame.model.GameCommand;
import de.hso.cardgame.model.Player;
import de.hso.cardgame.model.Rank;
import de.hso.cardgame.model.Suit;
import de.hso.cardgame.model.GameCommand.DealHands;
import de.hso.cardgame.model.GameCommand.PlayCard;
import de.hso.cardgame.model.GameCommand.RegisterPlayer;
import de.hso.cardgame.model.Hand;

public class GameCommandSerializer {

    /**
     * Serialisiert ein GameCommand in einen JSON-String.
     */
    public static String toJSON(GameCommand command) {
        // Wir erstellen ein JsonObject, in das wir Felder eintragen
        JsonObject obj = new JsonObject();

        if (command instanceof RegisterPlayer rp) {
            // Für RegisterPlayer legen wir ein type-Feld und ein name-Feld an
            obj.addProperty("type", "RegisterPlayer");
            obj.addProperty("name", rp.name());

        } else if (command instanceof DealHands dh)  {
            // 1) type-Feld
            obj.addProperty("type", "DealHands");

            // 2) Karteninformationen
            //    Wir legen ein JsonObject an, das alle Spieler->Karten abbildet
            JsonObject cardsObj = new JsonObject();
            
            for (com.github.andrewoma.dexx.collection.Pair<Player, Hand> entry : dh.cards()) {
                
            	JsonObject handJson = new JsonObject();
                JsonArray cardsArray = new JsonArray();

                for (Card card : entry.component2().cards()) {
                    JsonObject cardJson = new JsonObject();
                    cardJson.addProperty("suit", card.suit().toString());
                    cardJson.addProperty("rank", card.rank().value());
                    cardsArray.add(cardJson);
                }
 
                handJson.add("cards", cardsArray);
                cardsObj.add(entry.component1().toString(), handJson);
            }


            // Hänge das cards-JsonObject an das Hauptobjekt an
            obj.add("cards", cardsObj);;
        	
        } else if (command instanceof PlayCard pc) {
       
            obj.addProperty("type", "PlayCard");
            obj.addProperty("player", pc.player().toString());
            
            JsonObject cardJson = new JsonObject();
            cardJson.addProperty("suit", pc.card().suit().toString());
            cardJson.addProperty("rank", pc.card().rank().value());
            obj.add("card", cardJson);
        	
        }
        
        
        else {
            throw new IllegalArgumentException("Unbekannter Command-Typ: " + command.getClass().getSimpleName());
        }
        return obj.toString();
    }

    /**
     * Deserialisiert einen JSON-String in ein entsprechendes GameCommand-Objekt.
     * (hier nur angedeutet, kommt später noch dazu)
     */
    public static GameCommand fromJSON(String json) {
        var obj = JsonParser.parseString(json).getAsJsonObject();
        String type = obj.get("type").getAsString();

        switch (type) {
            case "RegisterPlayer":
                String name = obj.get("name").getAsString();
                return new GameCommand.RegisterPlayer(name);
            case "DealHands":
            	JsonObject cardsObj = obj.getAsJsonObject("cards");
                Map<Player, Hand> result = Maps.of(); 

                // Iteriere über die Spieler-Keys (P1, P2, ...)
                for (String key : cardsObj.keySet()) {
                    Player player = Player.valueOf(key); // Enum Player aus dem Key
                    JsonObject jhand = cardsObj.get(key).getAsJsonObject();
                    
                    JsonArray cards = jhand.get("cards").getAsJsonArray();

                    // Baue das Set<Card> auf
                    Set<Card> cardSet = HashSet.empty();
                    
                    for (JsonElement elem : cards) {
                        JsonObject cardObj = elem.getAsJsonObject();
                        Suit suit = Suit.valueOf(cardObj.get("suit").getAsString());
                        int rankValue = cardObj.get("rank").getAsInt();
                        Card card = new Card(suit, new Rank(rankValue));
                        cardSet = cardSet.add(card);
                    }

                    // Erstelle die Hand und füge sie in die Map ein
                    Hand hand = new Hand(cardSet);
                    result = result.put(player, hand);
                }

                return new GameCommand.DealHands(result);
                
            case "PlayCard":
            	String playerstr = obj.get("player").getAsString();
            	Player player = Player.valueOf(playerstr);
            	
                JsonObject cardJson = obj.getAsJsonObject("card");
                Suit suit = Suit.valueOf(cardJson.get("suit").getAsString());
                Rank rank = new Rank(cardJson.get("rank").getAsInt());
                Card card = new Card(suit, rank);
                
                return new GameCommand.PlayCard(player, card);
                
            default:
                throw new IllegalArgumentException("Unbekannter Command-Typ: " + type);
        }
    }
    
    public static void main(String[] args) {
    	// zusätzliche Variablen
    	
        Player player1 = Player.P1;
        Set<Card> cards = Sets.of(new Card(Suit.Hearts, new Rank(10)))
            .add(new Card(Suit.Hearts, new Rank(8)));
        Hand hand4 = new Hand(cards);
    	Map<Player, Hand> mapDealHAnds = Maps.of(Player.P2, hand4);
    	
        // 1) Erzeuge ein RegisterPlayer-Command
        GameCommand registerCmd = new GameCommand.RegisterPlayer("Alice");
        GameCommand dealHandsCmd = new GameCommand.DealHands(mapDealHAnds);
        GameCommand playCardCmd = new GameCommand.PlayCard(player1, new Card(Suit.Hearts, new Rank(10)));
        
        // 2) Serialize (Command -> JSON)
        String json = GameCommandSerializer.toJSON(registerCmd);
        System.out.println("JSONrp (Command -> JSON):");
        System.out.println(json);
        
        String json2 = GameCommandSerializer.toJSON(dealHandsCmd);
        System.out.println("JSONdh (Command -> JSON):");
        System.out.println(json2);      
        
        String json3 = GameCommandSerializer.toJSON(playCardCmd);
        System.out.println("JSONpc (Command -> JSON):");
        System.out.println(json3);      

        // 3) Deserialize (JSON -> Command)
        GameCommand deserializedCmdrp = GameCommandSerializer.fromJSON(json);
        System.out.println("\nDeserialized Command (JSON -> Command):");
        System.out.println(deserializedCmdrp);
        
        GameCommand deserializedCmddh = GameCommandSerializer.fromJSON(json2);
        System.out.println("\nDeserialized Command (JSON -> Command):");
        System.out.println(deserializedCmddh);
        
        GameCommand deserializedCmdpc = GameCommandSerializer.fromJSON(json3);
        System.out.println("\nDeserialized Command (JSON -> Command):");
        System.out.println(deserializedCmdpc);
        
        
        
        // 4) Zusätzliche Prüfung
        if (deserializedCmdrp instanceof GameCommand.RegisterPlayer rp) {
            System.out.println("\nName im deserialisierten Command: " + rp.name());
        } else {
            System.out.println("Fehler: Unerwarteter Command-Typ!");
        }
    }
}
