# Tron eventsubscribe plugin

This is an implementation of Tron eventsubscribe model. 

* **api** module defines IPluginEventListener, a protocol between Java-tron and event plugin. 
* **app** module is an example for loading plugin, developers could use it for debugging.
* **kafkaplugin** module is the implementation for kafka, it implements IPluginEventListener, it receives events subscribed from Java-tron and relay events to kafka server. 

### Setup/Build

1. Clone the repo
2. Go to eventplugin `cd eventplugin` 
3. run `./gradlew build`

* This will produce one plugin zip, named `plugin-kafka-1.0.0.zip`, located in the `eventplugin/build/plugins/` directory.


### Edit **config.conf** of Java-tron， add the following fileds:
```
event.subscribe = {
    path = "/Users/tron/sourcecode/eventplugin/build/plugins/plugin-kafka-1.0.0.zip"
    server = "127.0.0.1:9092"
    topics = [
        {
          triggerName = "block"
          enable = true
          topic = "block"
        },
        {
          triggerName = "transaction"
          enable = true
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
}

```
 * **path**: is the absolute path of "plugin-kafka-1.0.0.zip"
 * **server**: Kafka server address
 * **topics**: each event type maps to one Kafka topic, we support four event types subscribing, block, transaction, contractlog and contractevent.
 * **triggerName**: the trigger type, the value can't be modified.
 * **enable**: plugin can receive nothing if the value is false.
 * **topic**: the value is the kafka topic to receive events. Make sure it has been created and Kafka process is running

##### Run Kafka

```
zookeeper-server-start /usr/local/etc/kafka/zookeeper.properties & kafka-server-start /usr/local/etc/kafka/server.properties
```

#### Create topics to receive events, the topic is defined in config.conf

```
kafka-topics --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic block
kafka-topics --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic transaction
kafka-topics --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic contractlog
kafka-topics --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic contractevent
```

### Load plugin in Java-tron
* add --es to command line, for example:
```
 java -jar FullNode.jar -p privatekey -c config.conf --es 
```

### Tron event subscribe model
* Please refer to https://github.com/tronprotocol/TIPs/issues/12
