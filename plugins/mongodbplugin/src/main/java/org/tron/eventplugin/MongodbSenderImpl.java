package org.tron.eventplugin;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.util.StringUtils;
import org.tron.mongodb.MongoConfig;
import org.tron.mongodb.MongoManager;
import org.tron.mongodb.MongoTemplate;

@Slf4j(topic = "event")
public class MongodbSenderImpl {

  private static MongodbSenderImpl instance = null;
  @Getter
  BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();
  private final ExecutorService service = new ThreadPoolExecutor(8, 8,
      0L, TimeUnit.MILLISECONDS, queue);

  private boolean loaded = false;
  @Getter
  private BlockingQueue<Object> triggerQueue = new LinkedBlockingQueue<>();

  private String blockTopic = "";
  private String transactionTopic = "";
  private String contractEventTopic = "";
  private String contractLogTopic = "";
  private String solidityTopic = "";
  private String solidityEventTopic = "";
  private String solidityLogTopic = "";

  private Thread triggerProcessThread;
  private boolean isRunTriggerProcessThread = true;

  private MongoManager mongoManager;
  private Map<String, MongoTemplate> mongoTemplateMap;

  private String dbName;
  private String dbUserName;
  private String dbPassword;
  private int version; // 1: no index, 2: has index

  private MongoConfig mongoConfig;

  public static MongodbSenderImpl getInstance() {
    if (Objects.isNull(instance)) {
      synchronized (MongodbSenderImpl.class) {
        if (Objects.isNull(instance)) {
          instance = new MongodbSenderImpl();
        }
      }
    }
    return instance;
  }

  public void setDBConfig(String dbConfig) {
    if (StringUtils.isNullOrEmpty(dbConfig)) {
      return;
    }

    String[] params = dbConfig.split("\\|");
    if (params.length != 3 && params.length != 4) {
      return;
    }

    dbName = params[0];
    dbUserName = params[1];
    dbPassword = params[2];
    version = 1;

    if (params.length == 4) {
      version = Integer.parseInt(params[3]);
    }

    loadMongoConfig();
  }

  public void setServerAddress(final String serverAddress) {
    if (StringUtils.isNullOrEmpty(serverAddress)) {
      return;
    }

    String[] params = serverAddress.split(":");
    if (params.length != 2) {
      return;
    }

    String mongoHostName = "";
    int mongoPort;

    try {
      mongoHostName = params[0];
      mongoPort = Integer.parseInt(params[1]);
    } catch (Exception e) {
      log.error("SetServerAddress failed", e);
      return;
    }

    if (Objects.isNull(mongoConfig)) {
      mongoConfig = new MongoConfig();
    }

    mongoConfig.setHost(mongoHostName);
    mongoConfig.setPort(mongoPort);
  }

  public void init() {
    if (loaded) {
      return;
    }

    if (Objects.isNull(mongoManager)) {
      mongoManager = new MongoManager();
      mongoManager.initConfig(mongoConfig);
    }

    mongoTemplateMap = new HashMap<>();
    createCollections();

    triggerProcessThread = new Thread(triggerProcessLoop);
    triggerProcessThread.start();
    loaded = true;
  }

  private void createCollections() {
    if (mongoConfig.enabledIndexes()) {
      Map<String, Boolean> indexOptions = new HashMap<>();
      indexOptions.put("blockNumber", true);
      mongoManager.createCollection(blockTopic, indexOptions);

      indexOptions = new HashMap<>();
      indexOptions.put("transactionId", true);
      mongoManager.createCollection(transactionTopic, indexOptions);

      indexOptions = new HashMap<>();
      indexOptions.put("latestSolidifiedBlockNumber", true);
      mongoManager.createCollection(solidityTopic, indexOptions);

      indexOptions = new HashMap<>();
      indexOptions.put("uniqueId", true);
      mongoManager.createCollection(solidityEventTopic, indexOptions);
      mongoManager.createCollection(contractEventTopic, indexOptions);

      indexOptions = new HashMap<>();
      indexOptions.put("uniqueId", true);
      indexOptions.put("contractAddress", false);
      mongoManager.createCollection(solidityLogTopic, indexOptions);
      mongoManager.createCollection(contractLogTopic, indexOptions);
    } else {
      mongoManager.createCollection(blockTopic);
      mongoManager.createCollection(transactionTopic);
      mongoManager.createCollection(contractLogTopic);
      mongoManager.createCollection(contractEventTopic);
      mongoManager.createCollection(solidityTopic);
      mongoManager.createCollection(solidityEventTopic);
      mongoManager.createCollection(solidityLogTopic);
    }

    createMongoTemplate(blockTopic);
    createMongoTemplate(transactionTopic);
    createMongoTemplate(contractLogTopic);
    createMongoTemplate(contractEventTopic);
    createMongoTemplate(solidityTopic);
    createMongoTemplate(solidityEventTopic);
    createMongoTemplate(solidityLogTopic);
  }

  private void loadMongoConfig() {
    if (Objects.isNull(mongoConfig)) {
      mongoConfig = new MongoConfig();
    }

    if (StringUtils.isNullOrEmpty(dbName)) {
      return;
    }

    Properties properties = new Properties();

    try {
      InputStream input = getClass().getClassLoader().getResourceAsStream("mongodb.properties");
      if (Objects.isNull(input)) {
        return;
      }
      properties.load(input);

      int connectionsPerHost = Integer.parseInt(properties.getProperty("mongo.connectionsPerHost"));
      int threadsAllowedToBlockForConnectionMultiplie = Integer.parseInt(
          properties.getProperty("mongo.threadsAllowedToBlockForConnectionMultiplier"));

      mongoConfig.setDbName(dbName);
      mongoConfig.setUsername(dbUserName);
      mongoConfig.setPassword(dbPassword);
      mongoConfig.setVersion(version);
      mongoConfig.setConnectionsPerHost(connectionsPerHost);
      mongoConfig.setThreadsAllowedToBlockForConnectionMultiplier(
          threadsAllowedToBlockForConnectionMultiplie);
    } catch (Exception e) {
      log.error("LoadMongoConfig failed", e);
    }
  }

  private MongoTemplate createMongoTemplate(final String collectionName) {

    MongoTemplate template = mongoTemplateMap.get(collectionName);
    if (Objects.nonNull(template)) {
      return template;
    }

    template = new MongoTemplate(mongoManager) {
      @Override
      protected String collectionName() {
        return collectionName;
      }

      @Override
      protected <T> Class<T> getReferencedClass() {
        return null;
      }
    };

    mongoTemplateMap.put(collectionName, template);

    return template;
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


  public void upsertEntityLong(MongoTemplate template, Object data, String indexKey) {
    String dataStr = (String) data;
    try {
      JSONObject jsStr = JSON.parseObject(dataStr);
      Long indexValue = jsStr.getLong(indexKey);
      if (indexValue != null) {
        template.upsertEntity(indexKey, indexValue, dataStr);
      } else {
        template.addEntity(dataStr);
      }
    } catch (Exception ex) {
      log.error("upsertEntityLong exception happened in parse object ", ex);
    }
  }

  public void upsertEntityString(MongoTemplate template, Object data, String indexKey) {
    String dataStr = (String) data;
    try {
      JSONObject jsStr = JSON.parseObject(dataStr);
      String indexValue = jsStr.getString(indexKey);
      if (indexValue != null) {
        template.upsertEntity(indexKey, indexValue, dataStr);
      } else {
        template.addEntity(dataStr);
      }
    } catch (Exception ex) {
      log.error("upsertEntityLong exception happened in parse object ", ex);
    }
  }

  public void handleBlockEvent(Object data) {
    if (blockTopic == null || blockTopic.isEmpty()) {
      return;
    }

    MongoTemplate template = mongoTemplateMap.get(blockTopic);
    if (Objects.nonNull(template)) {
      service.execute(() -> {
        if (mongoConfig.enabledIndexes()) {
          upsertEntityLong(template, data, "blockNumber");
        } else {
          template.addEntity((String) data);
        }
      });
    }
  }

  public void handleTransactionTrigger(Object data) {
    if (Objects.isNull(data) || Objects.isNull(transactionTopic)) {
      return;
    }

    MongoTemplate template = mongoTemplateMap.get(transactionTopic);
    if (Objects.nonNull(template)) {
      service.execute(() -> {
        if (mongoConfig.enabledIndexes()) {
          upsertEntityString(template, data, "transactionId");
        } else {
          template.addEntity((String) data);
        }
      });
    }
  }

  public void handleSolidityTrigger(Object data) {
    if (Objects.isNull(data) || Objects.isNull(solidityTopic)) {
      return;
    }

    MongoTemplate template = mongoTemplateMap.get(solidityTopic);
    if (Objects.nonNull(template)) {
      service.execute(() -> {
        if (mongoConfig.enabledIndexes()) {
          upsertEntityLong(template, data, "latestSolidifiedBlockNumber");
        } else {
          template.addEntity((String) data);
        }
      });
    }
  }

  public void handleInsertContractTrigger(MongoTemplate template, Object data, String indexKey) {
    if (mongoConfig.enabledIndexes()) {
      upsertEntityString(template, data, indexKey);
    } else {
      template.addEntity((String) data);
    }
  }

  // will not delete when removed is set to true
  public void handleContractLogTrigger(Object data) {
    if (Objects.isNull(data) || Objects.isNull(contractLogTopic)) {
      return;
    }

    MongoTemplate template = mongoTemplateMap.get(contractLogTopic);
    if (Objects.nonNull(template)) {
      service.execute(() -> handleInsertContractTrigger(template, data, "uniqueId"));
    }
  }

  public void handleContractEventTrigger(Object data) {
    if (Objects.isNull(data) || Objects.isNull(contractEventTopic)) {
      return;
    }

    MongoTemplate template = mongoTemplateMap.get(contractEventTopic);
    if (Objects.nonNull(template)) {
      service.execute(() -> {
        String dataStr = (String) data;
        if (dataStr.contains("\"removed\":true")) {
          try {
            JSONObject jsStr = JSON.parseObject(dataStr);
            String uniqueId = jsStr.getString("uniqueId");
            if (uniqueId != null) {
              template.delete("uniqueId", uniqueId);
            }
          } catch (Exception ex) {
            log.error("unknown exception happened in parse object ", ex);
          }
        } else {
          handleInsertContractTrigger(template, data, "uniqueId");
        }
      });
    }
  }

  public void handleSolidityLogTrigger(Object data) {
    if (Objects.isNull(data) || Objects.isNull(solidityLogTopic)) {
      return;
    }

    MongoTemplate template = mongoTemplateMap.get(solidityLogTopic);
    if (Objects.nonNull(template)) {
      service.execute(() -> handleInsertContractTrigger(template, data, "uniqueId"));
    }
  }

  public void handleSolidityEventTrigger(Object data) {
    if (Objects.isNull(data) || Objects.isNull(solidityEventTopic)) {
      return;
    }

    MongoTemplate template = mongoTemplateMap.get(solidityEventTopic);
    if (Objects.nonNull(template)) {
      service.execute(() -> handleInsertContractTrigger(template, data, "uniqueId"));
    }
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
            Thread.currentThread().interrupt();
            break;
          } catch (Exception ex) {
            log.error("unknown exception happened in process capsule loop", ex);
          } catch (Throwable throwable) {
            log.error("unknown throwable happened in process capsule loop", throwable);
          }
        }
      };

  public void close() {
    log.info("Closing MongodbSender...");
    if (triggerProcessThread != null) {
      triggerProcessThread.interrupt();
    }
    service.shutdown(); // Disable new tasks from being submitted
    try {
      // Wait a while for existing tasks to terminate
      if (!service.awaitTermination(60, java.util.concurrent.TimeUnit.SECONDS)) {
        service.shutdownNow(); // Cancel currently executing tasks
        // Wait a while for tasks to respond to being cancelled
        if (!service.awaitTermination(60, java.util.concurrent.TimeUnit.SECONDS)) {
          log.warn("Mongo triggerProcessThread did not terminate");
        }
      }
    } catch (InterruptedException ie) {
      // (Re-)Cancel if current thread also interrupted
      service.shutdownNow();
      // Preserve interrupt status
      Thread.currentThread().interrupt();
    }
    log.info("MongodbSender closed.");
  }
}
