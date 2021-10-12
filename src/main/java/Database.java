import com.mongodb.*;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.net.UnknownHostException;
import java.util.ArrayList;

public class Database {

    private MongoClient mongoClient;
    private MongoDatabase mainDatabase;
    private MongoCollection statsDB;

    public Database() {
        mongoClient = new MongoClient();

        mainDatabase = mongoClient.getDatabase("PartyGames");
        statsDB = mainDatabase.getCollection("PartyGames");
    }

    public void addDefaultPlayer(Player player) {
        DBObject object = new BasicDBObject("uuid", player.getUniqueId())
                .append("points", 0)
                .append("games", new ArrayList<>());

        statsDB.insertOne(object);
    }

    public void playerInDatabase(Player player) {
        Cursor cursor = (Cursor) statsDB.find(new BasicDBObject("uuid", player.getUniqueId()));

        if (!cursor.hasNext()) {
            System.out.println("Player not found!");
            System.out.println(cursor);
        }
    }
}
