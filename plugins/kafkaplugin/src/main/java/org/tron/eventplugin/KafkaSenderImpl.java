package org.tron.eventplugin;

import com.alibaba.fastjson.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

@Slf4j(topic = "event")
public class KafkaSenderImpl {

  private static KafkaSenderImpl instance = null;

  @Setter
  private String serverAddress = "";
  private boolean loaded = false;

  private final Map<Integer, KafkaProducer<String, String>> producerMap = new HashMap<>();

  @Getter
  private BlockingQueue<Object> triggerQueue = new LinkedBlockingQueue<>();

  private String blockTopic = "";
  private String transactionTopic = "";
  private String contractEventTopic = "";
  private String contractLogTopic = "";
  private String solidityTopic = "";
  private String solidityLogTopic = "";
  private String solidityEventTopic = "";

  private Thread triggerProcessThread;
  private boolean isRunTriggerProcessThread = true;

  public static KafkaSenderImpl getInstance() {
    if (Objects.isNull(instance)) {
      synchronized (KafkaSenderImpl.class) {
        if (Objects.isNull(instance)) {
          instance = new KafkaSenderImpl();
        }
      }
    }
    return instance;
  }

  public void init() {
    if (loaded) {
      return;
    }

    createProducer(EventTopic.BLOCK_TRIGGER.getType());
    createProducer(EventTopic.TRANSACTION_TRIGGER.getType());
    createProducer(EventTopic.CONTRACT_LOG_TRIGGER.getType());
    createProducer(EventTopic.CONTRACT_EVENT_TRIGGER.getType());
    createProducer(EventTopic.SOLIDITY_TRIGGER.getType());
    createProducer(EventTopic.SOLIDITY_EVENT.getType());
    createProducer(EventTopic.SOLIDITY_LOG.getType());

    triggerProcessThread = new Thread(triggerProcessLoop);
    triggerProcessThread.start();
    loaded = true;
  }

  public void setTopic(int triggerType, String topic) {
    EventTopic eventTopic = EventTopic.getEventTopicByType(triggerType);
    if (eventTopic == null) {
      log.error("Unknown trigger type {}", triggerType);
      return;
    }
    switch (eventTopic) {
      case BLOCK_TRIGGER:
        blockTopic = topic;
        break;
      case TRANSACTION_TRIGGER:
        transactionTopic = topic;
        break;
      case CONTRACT_EVENT_TRIGGER:
        contractEventTopic = topic;
        break;
      case CONTRACT_LOG_TRIGGER:
        contractLogTopic = topic;
        break;
      case SOLIDITY_TRIGGER:
        solidityTopic = topic;
        break;
      case SOLIDITY_EVENT:
        solidityEventTopic = topic;
        break;
      case SOLIDITY_LOG:
        solidityLogTopic = topic;
        break;
    }
  }

  private KafkaProducer<String, String> createProducer(int eventType) {
    KafkaProducer<String, String> producer;

    Thread currentThread = Thread.currentThread();
    ClassLoader savedClassLoader = currentThread.getContextClassLoader();

    currentThread.setContextClassLoader(null);

    Properties props = new Properties();
    props.put("acks", "all");
    props.put("retries", 0);
    props.put("linger.ms", 1);
    props.put("bootstrap.servers", this.serverAddress);
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

    producer = new KafkaProducer<String, String>(props);

    producerMap.put(eventType, producer);

    currentThread.setContextClassLoader(savedClassLoader);

    return producer;
  }

  private void printTimestamp(String data) {
    Date date = new Date();
    SimpleDateFormat ft = new SimpleDateFormat("hh:mm:ss:SSS");
    System.out.println(ft.format(date) + ": " + data);
  }

  public void sendKafkaRecord(int eventType, String kafkaTopic, Object data) {
    KafkaProducer<String, String> producer = producerMap.get(eventType);
    if (Objects.isNull(producer)) {
      return;
    }

    ProducerRecord<String, String> record = new ProducerRecord(kafkaTopic, data);
    try {
      producer.send(record, new Callback() {
        @Override
        public void onCompletion(RecordMetadata metadata, Exception exception) {
          log.debug("sendKafkaRecord successfully");
        }
      });
    } catch (Exception e) {
      log.error("sendKafkaRecord failed", e);
    }

    printTimestamp((String) data);
  }

  public void handleBlockEvent(Object data) {
    if (blockTopic == null || blockTopic.isEmpty()) {
      return;
    }
    KafkaSenderImpl.getInstance()
        .sendKafkaRecord(EventTopic.BLOCK_TRIGGER.getType(), blockTopic, data);
  }

  public void handleTransactionTrigger(Object data) {
    if (Objects.isNull(data) || Objects.isNull(transactionTopic)) {
      return;
    }
    KafkaSenderImpl.getInstance()
        .sendKafkaRecord(EventTopic.TRANSACTION_TRIGGER.getType(), transactionTopic, data);
  }

  public void handleContractLogTrigger(Object data) {
    if (Objects.isNull(data) || Objects.isNull(contractLogTopic)) {
      return;
    }
    KafkaSenderImpl.getInstance()
        .sendKafkaRecord(EventTopic.CONTRACT_LOG_TRIGGER.getType(), contractLogTopic, data);
  }

  public void handleContractEventTrigger(Object data) {
    if (Objects.isNull(data) || Objects.isNull(contractEventTopic)) {
      return;
    }
    KafkaSenderImpl.getInstance()
        .sendKafkaRecord(EventTopic.CONTRACT_EVENT_TRIGGER.getType(), contractEventTopic, data);
  }

  public void handleSolidityTrigger(Object data) {
    if (Objects.isNull(data) || Objects.isNull(solidityTopic)) {
      return;
    }
    KafkaSenderImpl.getInstance()
        .sendKafkaRecord(EventTopic.SOLIDITY_TRIGGER.getType(), solidityTopic, data);
  }

  public void handleSolidityLogTrigger(Object data) {
    if (Objects.isNull(data) || Objects.isNull(solidityLogTopic)) {
      return;
    }
    KafkaSenderImpl.getInstance()
        .sendKafkaRecord(EventTopic.SOLIDITY_LOG.getType(), solidityLogTopic, data);
  }

  public void handleSolidityEventTrigger(Object data) {
    if (Objects.isNull(data) || Objects.isNull(solidityEventTopic)) {
      return;
    }
    KafkaSenderImpl.getInstance()
        .sendKafkaRecord(EventTopic.SOLIDITY_EVENT.getType(), solidityEventTopic, data);
  }

  private final Runnable triggerProcessLoop =
      () -> {
        while (isRunTriggerProcessThread) {
          try {
            String triggerData = (String) triggerQueue.poll(1, TimeUnit.SECONDS);
            if (Objects.isNull(triggerData)) {
              continue;
            }
            //check if it's json
            JSONObject jsonObject = JSONObject.parseObject(triggerData);

            if (!jsonObject.containsKey("triggerName")) {
              log.error("Invalid triggerData without triggerName: {}", triggerData);
              continue;
            }
            String triggerName = jsonObject.getString("triggerName");
            EventTopic eventTopic = EventTopic.getEventTopicByName(triggerName);
            if (eventTopic == null) {
              log.error("Not matched triggerName {} in data {}", triggerName, triggerData);
              continue;
            }
            switch (eventTopic) {
              case BLOCK_TRIGGER:
                handleBlockEvent(triggerData);
                break;
              case TRANSACTION_TRIGGER:
                handleTransactionTrigger(triggerData);
                break;
              case CONTRACT_LOG_TRIGGER:
                handleContractLogTrigger(triggerData);
                break;
              case CONTRACT_EVENT_TRIGGER:
                handleContractEventTrigger(triggerData);
                break;
              case SOLIDITY_TRIGGER:
                handleSolidityTrigger(triggerData);
                break;
              case SOLIDITY_LOG:
                handleSolidityLogTrigger(triggerData);
                break;
              case SOLIDITY_EVENT:
                handleSolidityEventTrigger(triggerData);
                break;
            }
            log.debug("handle triggerData: {}", triggerData);
          } catch (InterruptedException ex) {
            log.info(ex.getMessage());
            Thread.currentThread().interrupt();
          } catch (Exception ex) {
            log.error("unknown exception happened in process capsule loop", ex);
          } catch (Throwable throwable) {
            log.error("unknown throwable happened in process capsule loop", throwable);
          }
        }
      };

  public void close() {
    for (Map.Entry<Integer, KafkaProducer<String, String>> entry : producerMap.entrySet()) {
      entry.getValue().close();
    }
    producerMap.clear();
  }
}
