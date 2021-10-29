package org.tron.eventplugin;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.BasicDBObject;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.util.JSON;
import java.util.stream.Collectors;
import org.bson.Document;
import org.pf4j.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.tron.mongodb.MongoConfig;
import org.tron.mongodb.MongoManager;
import org.tron.mongodb.MongoTemplate;

public class MongodbSenderImpl{
    private static MongodbSenderImpl instance = null;
    private static final Logger log = LoggerFactory.getLogger(MongodbSenderImpl.class);
    ExecutorService service = Executors.newFixedThreadPool(8);

    private boolean loaded = false;
    private BlockingQueue<Object> triggerQueue = new LinkedBlockingQueue();
    private Map<String, LinkedHashMap<String, JSONObject>> contractLogTriggersMap = new HashMap<>();

    private String blockTopic = "";
    private String transactionTopic = "";
    private String contractEventTopic = "";
    private String contractLogTopic = "";
    private String solidityTopic = "";

    private final String filterCollection = "filters";

    private final String contractLogCollectionFormat = "log_filter_%s";

    private final String blockNumberCollection = "block_num";

    private final String contractLogRevertCollectionFormat = "log_filter_revert_%s";

    private Thread triggerProcessThread;
    private boolean isRunTriggerProcessThread = true;

    private MongoManager mongoManager;
    private Map<String, MongoTemplate> mongoTemplateMap;

    private String dbName;
    private String dbUserName;
    private String dbPassword;

    private MongoConfig mongoConfig;

    public static MongodbSenderImpl getInstance(){
        if (Objects.isNull(instance)) {
            synchronized (MongodbSenderImpl.class){
                if (Objects.isNull(instance)){
                    instance = new MongodbSenderImpl();
                }
            }
        }

        return instance;
    }

    public void setDBConfig(String dbConfig){
        if (StringUtils.isNullOrEmpty(dbConfig)){
            return;
        }

        String[] params = dbConfig.split("\\|");
        if (params.length != 3){
            return;
        }

        dbName = params[0];
        dbUserName = params[1];
        dbPassword = params[2];

        loadMongoConfig();
    }

    public void setServerAddress(final String serverAddress){
        if (StringUtils.isNullOrEmpty(serverAddress)){
            return;
        }

        String[] params = serverAddress.split(":");
        if (params.length != 2){
            return;
        }

        String mongoHostName = "";
        int mongoPort = -1;

        try{
            mongoHostName = params[0];
            mongoPort = Integer.valueOf(params[1]);
        }
        catch (Exception e){
            e.printStackTrace();
            return;
        }

        if (Objects.isNull(mongoConfig)){
            mongoConfig = new MongoConfig();
        }

        mongoConfig.setHost(mongoHostName);
        mongoConfig.setPort(mongoPort);
    }

    public void init(){

        if (loaded){
            return;
        }

        if (Objects.isNull(mongoManager)){
            mongoManager = new MongoManager();
            mongoManager.initConfig(mongoConfig);
        }

        mongoTemplateMap = new HashMap<>();
        createCollections();

        triggerProcessThread = new Thread(triggerProcessLoop);
        triggerProcessThread.start();

        loaded = true;
    }

    private void createCollections(){
        mongoManager.createCollection(blockNumberCollection);
        createMongoTemplate(blockNumberCollection);

        mongoManager.createCollection(filterCollection);
        createMongoTemplate(filterCollection);
    }

    private void loadMongoConfig(){
        if (Objects.isNull(mongoConfig)){
            mongoConfig = new MongoConfig();
        }

        if (StringUtils.isNullOrEmpty(dbName)){
            return;
        }

        Properties properties = new Properties();

        try {
            InputStream input = getClass().getClassLoader().getResourceAsStream("mongodb.properties");
            if (Objects.isNull(input)){
                return;
            }
            properties.load(input);

            int connectionsPerHost = Integer.parseInt(properties.getProperty("mongo.connectionsPerHost"));
            int threadsAllowedToBlockForConnectionMultiplie = Integer.parseInt(
                    properties.getProperty("mongo.threadsAllowedToBlockForConnectionMultiplier"));

            mongoConfig.setDbName(dbName);
            mongoConfig.setUsername(dbUserName);
            mongoConfig.setPassword(dbPassword);
            mongoConfig.setConnectionsPerHost(connectionsPerHost);
            mongoConfig.setThreadsAllowedToBlockForConnectionMultiplier(threadsAllowedToBlockForConnectionMultiplie);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private MongoTemplate createMongoTemplate(final String collectionName){

        MongoTemplate template = mongoTemplateMap.get(collectionName);
        if (Objects.nonNull(template)){
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


    public void setTopic(int triggerType, String topic){
        if (triggerType == Constant.BLOCK_TRIGGER){
            blockTopic = topic;
        }
        else if (triggerType == Constant.TRANSACTION_TRIGGER){
            transactionTopic = topic;
        }
        else if (triggerType == Constant.CONTRACTEVENT_TRIGGER){
            contractEventTopic = topic;
        }
        else if (triggerType == Constant.CONTRACTLOG_TRIGGER){
            contractLogTopic = topic;
        } else if (triggerType == Constant.SOLIDITY_TRIGGER) {
            solidityTopic = topic;
        }
        else {
            return;
        }
    }

    public void close() {
    }

    public BlockingQueue<Object> getTriggerQueue(){
        return triggerQueue;
    }

    public void handleBlockEvent(Object data) {
        if (blockTopic == null || blockTopic.length() == 0){
            return;
        }
        JSONObject trigger = JSONObject.parseObject((String) data);
        long blockNumber = trigger.getLong("blockNumber");
        setBlockNumber(blockNumber, false);
    }

    public void handleTransactionTrigger(Object data) {
        if (Objects.isNull(data) || Objects.isNull(transactionTopic)){
            return;
        }
        JSONObject trigger = JSONObject.parseObject((String) data);
        String transactionId = trigger.getString("transactionId");
        LinkedHashMap<String, JSONObject> contractLogTriggers = contractLogTriggersMap.remove(transactionId);
        if (contractLogTriggers == null || contractLogTriggers.isEmpty()){
            return;
        }
        JSONObject firstTrigger = contractLogTriggers.values().iterator().next();
        long blockNumber = firstTrigger.getLong("blockNumber");

        Set<String> filterNameList = new HashSet<>();
        contractLogTriggers.values().forEach(
            logTrigger -> filterNameList.addAll(logTrigger.getObject("filterNameList", List.class))
        );

        for (String filterName : filterNameList){
            MongoTemplate template = getOrCreateLogFilterTemplate(filterName, false);
            if (Objects.nonNull(template)) {
                service.execute(new Runnable() {
                    @Override
                    public void run() {
                        List<Document> exists = template.queryByCondition(Filters.and(
                            Filters.eq("blockNumber", blockNumber),
                            Filters.eq("transactionId", transactionId)));
                        if(exists == null || exists.isEmpty()){
                            template.addList(contractLogTriggers.values().stream().map(Document::new).collect(Collectors.toList()));
                        }
                    }
                });
            }
        }
    }

    public void handleSolidityTrigger(Object data) {
        if (Objects.isNull(data) || Objects.isNull(solidityTopic)){
            return;
        }
        JSONObject trigger = JSONObject.parseObject((String) data);
        long blockNumber = trigger.getLong("latestSolidifiedBlockNumber");
        setBlockNumber(blockNumber, true);
    }

    public void handleContractLogTrigger(Object data) {
        if (Objects.isNull(data)){
            return;
        }
        JSONObject trigger = JSONObject.parseObject((String) data);
        String transactionId = trigger.getString("transactionId");
        LinkedHashMap<String, JSONObject> contractLogTriggers = contractLogTriggersMap
            .computeIfAbsent(transactionId, k -> new LinkedHashMap<>());
        contractLogTriggers.put(trigger.getString("uniqueId"), trigger);
    }

    public String getEventFilterList(){
        MongoTemplate template = mongoTemplateMap.get(filterCollection);
        if (Objects.nonNull(template)) {
            List<Document> filters = template.queryByCondition(BasicDBObject.parse("{disable : { $exists: false }}"));
            return JSON.serialize(filters);
        }
        return null;
    }

    private Runnable triggerProcessLoop =
            () -> {
                while (isRunTriggerProcessThread) {
                    try {
                        String triggerData = (String)triggerQueue.poll(1, TimeUnit.SECONDS);

                        if (Objects.isNull(triggerData)){
                            continue;
                        }

                        if (triggerData.contains(Constant.BLOCK_TRIGGER_NAME)){
                            handleBlockEvent(triggerData);
                        }
                        else if (triggerData.contains(Constant.TRANSACTION_TRIGGER_NAME)){
                            handleTransactionTrigger(triggerData);
                        }
                        else if (triggerData.contains(Constant.CONTRACTLOG_TRIGGER_NAME)){
                            handleContractLogTrigger(triggerData);
                        }
                        else if (triggerData.contains(Constant.SOLIDITY_TRIGGER_NAME)) {
                            handleSolidityTrigger(triggerData);
                        }
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

    public MongoTemplate getOrCreateLogFilterTemplate(String filterName, boolean forRevert) {
        String format = forRevert ? contractLogRevertCollectionFormat : contractLogCollectionFormat;
        String collection = String.format(format, filterName);
        MongoTemplate mongoTemplate = mongoTemplateMap.get(collection);
        if (mongoTemplate == null){
            mongoManager.createCollection(collection);
            mongoTemplate = createMongoTemplate(collection);
            mongoTemplate.createIndex(Indexes.ascending("blockNumber", "transactionId"), null);
        }
        return mongoTemplate;
    }

    public void setBlockNumber(long blockNumber, boolean solidity){
        MongoTemplate mongoTemplate = mongoTemplateMap.get(blockNumberCollection);
        mongoTemplate.updateMany(Filters.eq("is_solidity", solidity),
            new Document("$set", new Document("block_number", blockNumber)),
            new UpdateOptions().upsert(true));
    }

    public long getBlockNumber(boolean solidity){
        MongoTemplate mongoTemplate = mongoTemplateMap.get(blockNumberCollection);
        Document document = mongoTemplate.queryOne("is_solidity", solidity);
        if (document != null) {
            return document.getLong("block_number");
        } else {
            return 0;
        }
    }

}
