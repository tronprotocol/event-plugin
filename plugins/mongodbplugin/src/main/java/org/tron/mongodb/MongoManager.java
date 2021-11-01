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
import lombok.extern.slf4j.Slf4j;
import org.pf4j.util.StringUtils;

@Slf4j
public class MongoManager {

  private MongoClient mongo;
  private MongoDatabase db;

  public void initConfig(MongoConfig config) {
    int connectionsPerHost = config.getConnectionsPerHost();
    int threadsAllowedToBlockForConnectionMultiplier =
        config.getThreadsAllowedToBlockForConnectionMultiplier();
    MongoClientOptions options = MongoClientOptions.builder().connectionsPerHost(connectionsPerHost)
        .threadsAllowedToBlockForConnectionMultiplier(threadsAllowedToBlockForConnectionMultiplier)
        .build();

    String host = config.getHost();
    int port = config.getPort();
    ServerAddress serverAddress = new ServerAddress(host, port);
    List<ServerAddress> addrs = new ArrayList<ServerAddress>();
    addrs.add(serverAddress);

    String username = config.getUsername();
    String password = config.getPassword();
    String databaseName = config.getDbName();

    if (StringUtils.isNullOrEmpty(databaseName)) {
      return;
    }

    MongoCredential credential = MongoCredential.createScramSha1Credential(username, databaseName,
        password.toCharArray());
    List<MongoCredential> credentials = new ArrayList<MongoCredential>();
    credentials.add(credential);

    mongo = new MongoClient(addrs, credential, options);
    db = mongo.getDatabase(databaseName);
  }

  public void createCollection(String collectionName) {
    if (db != null && StringUtils.isNotNullOrEmpty(collectionName)) {
      if (Objects.isNull(db.getCollection(collectionName))){
        db.createCollection(collectionName);
      }
    }
  }

  public void createCollection(String collectionName, Map<String, Boolean> indexOptions) {
    log.info("[createCollection] collection={} start", collectionName);

    if (db != null && StringUtils.isNotNullOrEmpty(collectionName)) {
      List<String> collectionList = new ArrayList<>();
      db.listCollectionNames().into(collectionList);

      if (!collectionList.contains(collectionName)) {
        db.createCollection(collectionName);

        // create index
        if (indexOptions == null) {
          return;
        }
        for (String col : indexOptions.keySet()) {
          log.info("create index, col={}", col);
          db.getCollection(collectionName).createIndex(Indexes.ascending(col),
              new IndexOptions().name(col).unique(indexOptions.get(col)));
        }
      } else {
        log.info("[createCollection] collection={} already exists", collectionName);
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
