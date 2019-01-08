package org.tron.eventplugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.tron.mongodb.MongoConfig;
import org.tron.mongodb.MongoManager;
import org.tron.mongodb.MongoTemplate;

public class MongodbSenderImpl{
    private static MongodbSenderImpl instance = null;
    private static final Logger log = LoggerFactory.getLogger(MongodbSenderImpl.class);

    private String serverAddress = "";
    private boolean loaded = false;

    private BlockingQueue<Object> triggerQueue = new LinkedBlockingQueue();

    private String blockTopic = "";
    private String transactionTopic = "";
    private String contractEventTopic = "";
    private String contractLogTopic = "";

    private Thread triggerProcessThread;
    private boolean isRunTriggerProcessThread = true;

    private MongoManager mongoManager;
    private Map<String, MongoTemplate> mongoTemplateMap;

    private ObjectMapper mapper = new ObjectMapper();


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

    public void setServerAddress(String address){
        this.serverAddress = address;
    }

    public void init(){

        if (loaded){
            return;
        }

        triggerProcessThread = new Thread(triggerProcessLoop);
        triggerProcessThread.start();
        loaded = true;

        initMongoConfig();

    }

    private void initMongoConfig(){
        mongoManager = new MongoManager();
        mongoTemplateMap = new HashMap<>();

        MongoConfig config = new MongoConfig();
        config.setDbName("eventlog");
        config.setHost("127.0.0.1");
        config.setPort(27017);
        config.setUsername("tron");
        config.setPassword("123456");

        mongoManager.initConfig(config);
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
        }
        else {
            return;
        }

        mongoManager.createCollection(topic);
        createMongoTemplate(topic);
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

        MongoTemplate template = mongoTemplateMap.get(blockTopic);
        if (Objects.nonNull(template)){
            template.addEntity((String)data);
        }
    }

    public void handleTransactionTrigger(Object data) {
        if (Objects.isNull(data) || Objects.isNull(transactionTopic)){
            return;
        }

        MongoTemplate template = mongoTemplateMap.get(transactionTopic);
        if (Objects.nonNull(template)){
            template.addEntity((String)data);
        }
    }

    public void handleContractLogTrigger(Object data) {
        if (Objects.isNull(data) || Objects.isNull(contractLogTopic)){
            return;
        }

        MongoTemplate template = mongoTemplateMap.get(contractLogTopic);
        if (Objects.nonNull(template)){
            template.addEntity((String)data);
        }
    }

    public void handleContractEventTrigger(Object data) {
        if (Objects.isNull(data) || Objects.isNull(contractEventTopic)){
            return;
        }

        MongoTemplate template = mongoTemplateMap.get(contractEventTopic);
        if (Objects.nonNull(template)){
            template.addEntity((String)data);
        }
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
                        else if (triggerData.contains(Constant.CONTRACTEVENT_TRIGGER_NAME)){
                            handleContractEventTrigger(triggerData);
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
}
