package org.tron.mongodb;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.pf4j.util.StringUtils;

public class MongoManager {

    private MongoClient mongo;
    private MongoDatabase db;

    public void initConfig(MongoConfig config) {
        int connectionsPerHost = config.getConnectionsPerHost();
        int threadsAllowedToBlockForConnectionMultiplier = config.getThreadsAllowedToBlockForConnectionMultiplier();
        MongoClientOptions options = MongoClientOptions.builder().connectionsPerHost(connectionsPerHost)
                .threadsAllowedToBlockForConnectionMultiplier(threadsAllowedToBlockForConnectionMultiplier).build();

        String host = config.getHost();
        int port = config.getPort();
        ServerAddress serverAddress = new ServerAddress(host, port);
        List<ServerAddress> addrs = new ArrayList<ServerAddress>();
        addrs.add(serverAddress);

        String username = config.getUsername();
        String password = config.getPassword();
        String databaseName = config.getDbName();

        if (StringUtils.isNullOrEmpty(databaseName)){
            return;
        }

        MongoCredential credential = MongoCredential.createScramSha1Credential(username, databaseName,
                password.toCharArray());
        List<MongoCredential> credentials = new ArrayList<MongoCredential>();
        credentials.add(credential);

        mongo = new MongoClient(addrs, credential, options);
        db = mongo.getDatabase(databaseName);
    }

    public void createCollection(String collectionName, Map<String, Boolean> col2unique) {
        if (db != null && StringUtils.isNotNullOrEmpty(collectionName)) {
            if (Objects.isNull(db.getCollection(collectionName))) {
                db.createCollection(collectionName);
                if (col2unique == null) {
                  return;
                }
                for (String col : col2unique.keySet()) {
                  db.getCollection(collectionName).createIndex(Indexes.ascending(col),
                      new IndexOptions().unique(col2unique.get(col)));
                }
            }
        }
    }

    public MongoClient getMongo() {
        return mongo;
    }

    public void setMongo(MongoClient mongo) {
        this.mongo = mongo;
    }

    public MongoDatabase getDb() {
        return db;
    }

    public void setDb(MongoDatabase db) {
        this.db = db;
    }

}
