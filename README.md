# Tron eventsubscribe plugin

This is an implementation of Tron eventsubscribe model. 

* **api** module defines IPluginEventListener, a protocol between Java-tron and event plugin. 
* **app** module is an example for loading plugin, developers could use it for debugging.
* **kafkaplugin** module is the implementation for kafka, it implements IPluginEventListener, it receives events subscribed from Java-tron and relay events to kafka server. 
* **mongodbplugin** mongodbplugin module is the implementation for mongodb. 
### Setup/Build

1. Clone the repo
2. Go to eventplugin `cd eventplugin` 
3. run `./gradlew build`

* This will produce one plugin zip, named `plugin-kafka-1.0.0.zip`, located in the `eventplugin/build/plugins/` directory.


### Edit **config.conf** of Java-tron， add the following fileds:
```
event.subscribe = {
    path = "" // absolute path of plugin
    server = "" // target server address to receive event triggers
    dbconfig="" // dbname|username|password
    topics = [
        {
          triggerName = "block" // block trigger, the value can't be modified
          enable = false
          topic = "block" // plugin topic, the value could be modified
        },
        {
          triggerName = "transaction"
          enable = false
          topic = "transaction"
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
 * **path**: is the absolute path of "plugin-kafka-1.0.0.zip"
 * **server**: Kafka server address
 * **topics**: each event type maps to one Kafka topic, we support four event types subscribing, block, transaction, contractlog and contractevent.
 * **dbconfig**: db configuration information for mongodb, if using kafka, delete this one; if using Mongodb, add like that dbname|username|password
 * **triggerName**: the trigger type, the value can't be modified.
 * **enable**: plugin can receive nothing if the value is false.
 * **topic**: the value is the kafka topic to receive events. Make sure it has been created and Kafka process is running  
 * **filter**: filter condition for process trigger.
 **note**: if the server is not 127.0.0.1, pls set some properties in config/server.properties file  
           remove comment and set listeners=PLAINTEXT://:9092  
           remove comment and set advertised.listeners to PLAINTEXT://host_ip:9092 

##### Install Kafka
**On Mac**:
```
brew install kafka
```

**On Linux**:
```
cd /usr/local
wget http://archive.apache.org/dist/kafka/0.10.2.2/kafka_2.10-0.10.2.2.tgz
tar -xzvf kafka_2.10-0.10.2.2.tgz 
mv kafka_2.10-0.10.2.2 kafka

add "export PATH=$PATH:/usr/local/kafka/bin" to end of /etc/profile
source /etc/profile


kafka-server-start.sh /usr/local/kafka/config/server.properties &

```
**Note**: make sure the version of Kafka is the same as the version set in build.gradle of eventplugin project.(kafka_2.10-0.10.2.2 kafka)

##### Run Kafka
**On Mac**:
```
zookeeper-server-start /usr/local/etc/kafka/zookeeper.properties & kafka-server-start /usr/local/etc/kafka/server.properties
```

**On Linux**:
```
zookeeper-server-start.sh /usr/local/kafka/config/zookeeper.properties &
Sleep about 3 seconds 
kafka-server-start.sh /usr/local/kafka/config/server.properties &
```

#### Create topics to receive events, the topic is defined in config.conf

**On Mac**:
```
kafka-topics --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic block
kafka-topics --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic transaction
kafka-topics --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic contractlog
kafka-topics --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic contractevent
```

**On Linux**:
```
kafka-topics.sh  --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic block
kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic transaction
kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic contractlog
kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic contractevent
```

#### Kafka consumer

**On Mac**:
```
kafka-console-consumer --bootstrap-server localhost:9092  --topic block
kafka-console-consumer --bootstrap-server localhost:9092  --topic transaction
kafka-console-consumer --bootstrap-server localhost:9092  --topic contractlog
kafka-console-consumer --bootstrap-server localhost:9092  --topic contractevent
```

**On Linux**:
```
kafka-console-consumer.sh --zookeeper localhost:2181 --topic block
kafka-console-consumer.sh --zookeeper localhost:2181 --topic transaction
kafka-console-consumer.sh --zookeeper localhost:2181 --topic contractlog
kafka-console-consumer.sh --zookeeper localhost:2181 --topic contractevent
```

### Load plugin in Java-tron
* add --es to command line, for example:
```
 java -jar FullNode.jar -p privatekey -c config.conf --es 
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
