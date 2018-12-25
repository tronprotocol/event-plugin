# Tron eventsubscribe plugin

### Setup/Build

1. Clone the repo
2. Go to eventplugin `cd eventplugin` 
3. run `./gradlew build`

* This will produce one plugin zip, named `plugin-kafka-1.0.0.zip`, located in the `eventplugin/build/plugins/` directory.

### Configuration

1. Edit **config.conf** of Java-tron， add the following fileds:
```
event.subscribe = {
    path = "/Users/tron/sourcecode/eventplugin/plugins/kafkaplugin/build/libs/plugin-kafka-1.0.0.zip"
    server = "127.0.0.1:9092"
    topics = [
        {
          triggerName = "block"
          enable = false
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

### Load plugin
* add --es to command line
* Example: 
```
 java -jar FullNode.jar -p privatekey -c config.conf --es 
```

### Tron event subscribe model
* Please refer to https://github.com/tronprotocol/TIPs/issues/12
