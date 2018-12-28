package org.tron.eventplugin;
import org.apache.kafka.clients.producer.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class MessageSenderImpl{
    private static MessageSenderImpl instance = null;
    private static final Logger log = LoggerFactory.getLogger(MessageSenderImpl.class);

    private String serverAddress = "";
    private boolean loaded = false;

    private Map<Integer, KafkaProducer> producerMap = new HashMap<>();

    private BlockingQueue<Object> triggerQueue = new LinkedBlockingQueue();

    private String blockTopic = "";
    private String transactionTopic = "";
    private String contractEventTopic = "";
    private String contractLogTopic = "";

    private Thread triggerProcessThread;
    private boolean isRunTriggerProcessThread = true;


    public static MessageSenderImpl getInstance(){
        if (Objects.isNull(instance)) {
            synchronized (MessageSenderImpl.class){
                if (Objects.isNull(instance)){
                    instance = new MessageSenderImpl();
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

        createProducer(Constant.BLOCK_TRIGGER);
        createProducer(Constant.TRANSACTION_TRIGGER);
        createProducer(Constant.CONTRACTLOG_TRIGGER);
        createProducer(Constant.CONTRACTEVENT_TRIGGER);

        triggerProcessThread = new Thread(triggerProcessLoop);
        triggerProcessThread.start();

        loaded = true;
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
    }


    private KafkaProducer createProducer(int eventType){

        KafkaProducer producer = null;

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

    public void sendKafkaRecord(int eventType, String kafkaTopic, Object data){
        System.out.println(data);

        KafkaProducer producer = producerMap.get(eventType);
        if (Objects.isNull(producer)){
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
           log.error("sendKafkaRecord {}", e);
        }
    }

    public void close() {
        for (Map.Entry<Integer, KafkaProducer> entry: producerMap.entrySet()){
            entry.getValue().close();
        }

        producerMap.clear();
    }

    public BlockingQueue<Object> getTriggerQueue(){
        return triggerQueue;
    }

    public void handleBlockEvent(Object data) {
        if (blockTopic == null || blockTopic.length() == 0){
            return;
        }

        MessageSenderImpl.getInstance().sendKafkaRecord(Constant.BLOCK_TRIGGER, blockTopic, data);
    }

    public void handleTransactionTrigger(Object data) {
        if (Objects.isNull(data) || Objects.isNull(transactionTopic)){
            return;
        }

        MessageSenderImpl.getInstance().sendKafkaRecord(Constant.TRANSACTION_TRIGGER, transactionTopic, data);
    }

    public void handleContractLogTrigger(Object data) {
        if (Objects.isNull(data) || Objects.isNull(contractLogTopic)){
            return;
        }

        MessageSenderImpl.getInstance().sendKafkaRecord(Constant.CONTRACTLOG_TRIGGER, contractLogTopic, data);
    }

    public void handleContractEventTrigger(Object data) {
        if (Objects.isNull(data) || Objects.isNull(contractEventTopic)){
            return;
        }

        MessageSenderImpl.getInstance().sendKafkaRecord(Constant.CONTRACTEVENT_TRIGGER, contractEventTopic, data);
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
