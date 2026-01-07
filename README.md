# Tron event subscribe plugin

This is an implementation of Tron event subscribe model. 

* **api** module defines IPluginEventListener, a protocol between Java-tron and event plugin. 
* **app** module is an example for loading plugin, developers could use it for debugging.
* **kafkaplugin** module is the implementation for kafka, it implements IPluginEventListener, it receives events subscribed from Java-tron and relay events to kafka server. 
* **mongodbplugin** mongodbplugin module is the implementation for mongodb. 
### Setup/Build
Event-plugin can be built with JDK 8 or JDK 17.
1. Clone the repo
2. Go to eventplugin `cd eventplugin` 
3. run `./gradlew build`

* This will produce plugin zips, named `plugin-kafka-1.0.0.zip` and `plugin-mongodb-1.0.0.zip`, located in the `eventplugin/build/plugins/` directory.


### Edit **config.conf** of Java-tron, add the following fields:
```
event.subscribe = {
    path = "" // absolute path of plugin
    server = "" // target server address to receive event triggers
    # dbname|username|password or dbname|username|password|version
    # if you use version 2 and one collection not exists, it will create index automaticaly;
    # In any other case, it will not create index, you must create index manually
    dbconfig = ""
    topics = [
        {
          triggerName = "block" // block trigger, the value can't be modified
          enable = false
          topic = "block" // plugin topic, the value could be modified
          solidified = true // if set true, just need solidified block, default is false
        },
        {
          triggerName = "transaction"
          enable = false
          topic = "transaction"
          solidified = true
          ethCompatible = true // if set true, add transactionIndex, cumulativeEnergyUsed, preCumulativeLogCount, logList, energyUnitPrice, default is false
        },
        {
          triggerName = "contractevent"
          enable = true
          topic = "contractevent"
        },
        {
          triggerName = "contractlog"
          enable = true
          topic = "contractlog"
          redundancy = true // if set true, contractevent will also be regarded as contractlog
        },
        {
          triggerName = "solidity" // solidity block trigger(just include solidity block number and timestamp), the value can't be modified
          enable = true            // the default value is true
          topic = "solidity"
        },
        {
          triggerName = "solidityevent"
          enable = false
          topic = "solidityevent"
        },
        {
          triggerName = "soliditylog"
          enable = false
          topic = "soliditylog"
          redundancy = true // if set true, solidityevent will also be regarded as soliditylog
        }
    ]

    filter = {
       fromblock = "" // the value could be "", "earliest" or a specified block number as the beginning of the queried range
       toblock = "" // the value could be "", "latest" or a specified block number as end of the queried range
       contractAddress = [
           "" // contract address you want to subscribe, if it's set to "", you will receive contract logs/events with any contract address.
       ]

       contractTopic = [
           "" // contract topic you want to subscribe, if it's set to "", you will receive contract logs/events with any contract topic.
       ]
    }
}


```
 * **path**: is the absolute path of "plugin-kafka-1.0.0.zip" or "plugin-mongodb-1.0.0.zip"
 * **server**: Kafka(or MongoDB) server address, the default port is 9092(MongoDB is 27017)
 * **dbconfig**: db configuration information for mongodb, if using kafka, delete this one; if using Mongodb, add like that dbname|username|password or dbname|username|password|version if you want to create indexes when init
 * **topics**: each event type maps to one Kafka topic(or MongoDB collection), we support seven event types subscribing, block, transaction, contractlog, contractevent, solidity, soliditylog and solidityevent.   
   **triggerName**: the trigger type, the value can't be modified.  
   **enable**: plugin can receive nothing if the value is false.  
   **topic**: the value is the kafka topic to receive events. Make sure it has been created and Kafka process is running  
   **solidified**: if just need solidified data, just works for block and transaction  
   **redundancy**: if will also trigger event as log, just works for contractlog and soliditylog   
   **ethCompatible**: if set to true, will add some fields to transaction: transactionIndex, cumulativeEnergyUsed, preCumulativeLogCount, logList, energyUnitPrice
   
 * **filter**: filter condition for process trigger.
 **note**: if the server is not 127.0.0.1, pls set some properties in config/server.properties file  
           remove comment and set listeners=PLAINTEXT://:9092  
           remove comment and set advertised.listeners to PLAINTEXT://host_ip:9092 

### How to use kafka plugin
##### Install Kafka
*On Mac*:
```
brew install kafka
```

*On Linux*:
```
cd /usr/local
wget http://archive.apache.org/dist/kafka/0.10.2.2/kafka_2.10-0.10.2.2.tgz
tar -xzvf kafka_2.10-0.10.2.2.tgz 
mv kafka_2.10-0.10.2.2 kafka

add "export PATH=$PATH:/usr/local/kafka/bin" to end of /etc/profile
source /etc/profile

```
**Note**: make sure the version of Kafka is the same as the version set in build.gradle of eventplugin project.(kafka_2.10-0.10.2.2 kafka)

##### Run Kafka
*On Mac*:
```
zookeeper-server-start /usr/local/etc/kafka/zookeeper.properties & kafka-server-start /usr/local/etc/kafka/server.properties
```

*On Linux*:
```
zookeeper-server-start.sh /usr/local/kafka/config/zookeeper.properties &
Sleep about 3 seconds 
kafka-server-start.sh /usr/local/kafka/config/server.properties &
```

#### Create topics to receive events, the topic is defined in config.conf

*On Mac*:
```
kafka-topics --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic block
kafka-topics --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic transaction
kafka-topics --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic contractlog
kafka-topics --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic contractevent
kafka-topics --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic solidity
kafka-topics --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic solidityevent
kafka-topics --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic soliditylog
```

*On Linux*:
```
kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic block
kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic transaction
kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic contractlog
kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic contractevent
kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic solidity
kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic solidityevent
kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic soliditylog
```

#### Kafka consumer

*On Mac*:
```
kafka-console-consumer --bootstrap-server localhost:9092  --topic block
kafka-console-consumer --bootstrap-server localhost:9092  --topic transaction
kafka-console-consumer --bootstrap-server localhost:9092  --topic contractlog
kafka-console-consumer --bootstrap-server localhost:9092  --topic contractevent
kafka-console-consumer --bootstrap-server localhost:9092  --topic solidity
kafka-console-consumer --bootstrap-server localhost:9092  --topic solidityevent
kafka-console-consumer --bootstrap-server localhost:9092  --topic soliditylog
```

*On Linux*:
```
kafka-console-consumer.sh --zookeeper localhost:2181 --topic block
kafka-console-consumer.sh --zookeeper localhost:2181 --topic transaction
kafka-console-consumer.sh --zookeeper localhost:2181 --topic contractlog
kafka-console-consumer.sh --zookeeper localhost:2181 --topic contractevent
kafka-console-consumer.sh --zookeeper localhost:2181 --topic solidity
kafka-console-consumer.sh --zookeeper localhost:2181 --topic solidityevent
kafka-console-consumer.sh --zookeeper localhost:2181 --topic soliditylog
```

See more details on [developers](https://developers.tron.network/docs/event-plugin-deployment-kafka).

### How to use MongoDB plugin
These are default indexes when build automatically:
```
db.block.createIndex({ blockNumber: 1 },{ name: "blockNumber",unique: true});

db.transaction.createIndex({ transactionId: 1 },{ name: "transactionId",unique: true });

db.solidity.createIndex({ latestSolidifiedBlockNumber: 1 },{ name: "latestSolidifiedBlockNumber",unique: true });

db.solidityevent.createIndex({ uniqueId: 1 },{ name: "uniqueId",unique: true });

db.contractevent.createIndex({ uniqueId: 1 },{ name: "uniqueId",unique: true });

db.soliditylog.createIndex({ uniqueId: 1 },{ name: "uniqueId",unique: true });
db.soliditylog.createIndex({ contractAddress: 1 },{ name: "contractAddress" });

db.contractlog.createIndex({ uniqueId: 1 },{ name: "uniqueId",unique: true });
db.contractlog.createIndex({ contractAddress: 1 },{ name: "contractAddress" });
```
You can also create other indexes as necessary. See more details on [developers](https://developers.tron.network/docs/event-plugin-deployment-mongodb).

### Load plugin in Java-tron
* add --es to command line, for example:
```
 java -jar FullNode.jar -c config.conf --es 
```


### Event filter
which is defined in config.conf, path: event.subscribe
```
filter = {
       fromblock = "" // the value could be "", "earliest" or a specified block number as the beginning of the queried range
       toblock = "" // the value could be "", "latest" or a specified block number as end of the queried range
       contractAddress = [
           "TVkNuE1BYxECWq85d8UR9zsv6WppBns9iH" // contract address you want to subscribe, if it's set to "", you will receive contract logs/events with any contract address.
       ]

       contractTopic = [
           "f0f1e23ddce8a520eaa7502e02fa767cb24152e9a86a4bf02529637c4e57504b" // contract topic you want to subscribe, if it's set to "", you will receive contract logs/events with any contract topic.
       ]
    }
```
