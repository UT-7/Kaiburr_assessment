package server;

import static spark.Spark.*;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.ConnectionString;
import com.google.gson.Gson;

public class server {

    //private static final String DATABASE_NAME = "mydb";
    //private static final String COLLECTION_NAME = "servers";
    private static final String DATABASE_NAME = System.getenv("DB");
    private static final String COLLECTION_NAME = System.getenv("COLL");
    //private static final String CONN_STR = "mongodb://mongodb-dev.default.svc.cluster.local";
    //private static final ConnectionString CS = new ConnectionString("mongodb://root:password098@mongodb-dev.default.svc.cluster.local/?authSource=mydb");
    //private static final String USR = "root";
    private static final String USR = System.getenv("USR");
    //private static final String PSWD = "password098";
    private static final String PSWD = System.getenv("PSWD");
    //private static final String HOST = "mongodb-dev";
    private static final String HOST = System.getenv("HOST");
    private static final ConnectionString CS = new ConnectionString("mongodb://" + USR + ":" + PSWD + "@" + HOST + "/");

    public static void main(String[] args) {

        // Initialize MongoDB connection
        MongoDatabase db = MongoClients.create(CS).getDatabase(DATABASE_NAME);
        MongoCollection<Document> collection = db.getCollection(COLLECTION_NAME);

        // Endpoint for getting all servers or a single server by ID
        port(8080);
        Gson gson = new Gson();
        System.out.println("Starting server.");
        get("/servers", (req, res) -> {
            String id = req.queryParams("id");
            if (id == null) {
                // Return all servers
                List<Document> servers = new ArrayList<>();
                try (MongoCursor<Document> cursor = collection.find().projection(Projections.excludeId()).iterator()) {
                    while (cursor.hasNext()) {
                        servers.add(cursor.next());
                    }
                }
                return gson.toJson(servers);
            } else {
                // Return a single server by ID
                Document server = collection.find(Filters.eq("id", id)).projection(Projections.excludeId()).first();
                if (server == null) {
                    res.status(404);
                    return "Server not found";
                } else {
                    return gson.toJson(server);
                }
            }
        });

        // Endpoint for creating a new server
        put("/servers", (req, res) -> {
        	System.out.println("Received put request.");
        	System.out.println(req.body());
            try {
            	Document server = Document.parse(req.body());
            	collection.insertOne(server);
            } catch(Exception e) {
            	res.status(500);
            	return e.toString();
            }
            res.status(201);
            return "Server created";
        });

        // Endpoint for deleting a server by ID
        delete("/servers/:id", (req, res) -> {
            String id = req.params("id");
            long deletedCount = collection.deleteOne(Filters.eq("id", id)).getDeletedCount();
            if (deletedCount == 0) {
                res.status(404);
                return "Server not found";
            } else {
                return "Server deleted";
            }
        });

        // Endpoint for finding servers by name
        get("/servers/find/:name", (req, res) -> {
            String name = req.params("name");
            List<Document> servers = new ArrayList<>();
            try (MongoCursor<Document> cursor = collection.find(Filters.regex("name", name)).projection(Projections.excludeId()).iterator()) {
                while (cursor.hasNext()) {
                    servers.add(cursor.next());
                }
            }
            if (servers.isEmpty()) {
                res.status(404);
                return "No servers found";
            } else {
                return gson.toJson(servers);
            }
        });
    }
}
