package org.tron.mongodb;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MongoConfig {

  private String host;
  private int port = 27017;
  private String dbName;
  private String username;
  private String password;
  private int version;
  private int connectionsPerHost = 10;
  private int threadsAllowedToBlockForConnectionMultiplier = 10;

  public boolean enabledIndexes() {
    return version == 2;
  }
}
