package org.tron.eventplugin;

import org.apache.kafka.clients.producer.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tron.common.logsfilter.trigger.BlockLogTrigger;
import org.tron.common.logsfilter.trigger.ContractEventTrigger;
import org.tron.common.logsfilter.trigger.ContractLogTrigger;
import org.tron.common.logsfilter.trigger.TransactionLogTrigger;
import java.util.*;

public class MessageSenderImpl{
    private static MessageSenderImpl instance = null;
    private static final Logger log = LoggerFactory.getLogger(MessageSenderImpl.class);

    private String serverAddress = "";

    private static final String VALUE_SERIALIZER = "value.serializer";

    private boolean loaded = false;

    private Map<Integer, KafkaProducer> producerMap = new HashMap<>();

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

        loaded = true;
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

        if (eventType == Constant.BLOCK_TRIGGER){
            props.put(VALUE_SERIALIZER, "org.tron.eventplugin.serializer.BlockLogSerializer");
            producer = new KafkaProducer<String, BlockLogTrigger>(props);
        }
        else if (eventType == Constant.TRANSACTION_TRIGGER){
            props.put(VALUE_SERIALIZER, "org.tron.eventplugin.serializer.TransactionLogSerializer");
            producer = new KafkaProducer<String, TransactionLogTrigger>(props);
        }
        else if (eventType == Constant.CONTRACTLOG_TRIGGER){
            props.put(VALUE_SERIALIZER, "org.tron.eventplugin.serializer.ContractLogSerializer");
            producer = new KafkaProducer<String, ContractLogTrigger>(props);
        }
        else if (eventType == Constant.CONTRACTEVENT_TRIGGER){
            props.put(VALUE_SERIALIZER, "org.tron.eventplugin.serializer.ContractEventSerializer");
            producer = new KafkaProducer<String, ContractEventTrigger>(props);
        }
        else {
            log.error("unknown event type");
            return null;
        }

        producerMap.put(eventType, producer);

        currentThread.setContextClassLoader(savedClassLoader);

        return producer;
    }

    public void sendKafkaRecord(int eventType, String kafkaTopic, Object data){
        log.debug("sendKafkaRecord: topic={}, data={}", kafkaTopic, data);

        KafkaProducer producer = producerMap.get(eventType);
        if (Objects.isNull(producer)){
            return;
        }

        ProducerRecord<String, BlockLogTrigger> record = new ProducerRecord(kafkaTopic, data);
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

    public static void main(String[] args){
        MessageSenderImpl.getInstance().init();

        BlockLogTrigger trigger = new BlockLogTrigger();
        trigger.setBlockHash("block hash");
        trigger.setTimeStamp(System.currentTimeMillis());
        trigger.setTransactionSize(1000);

        for (int index = 0; index < 1000; ++index){
            trigger.setBlockNumber(index);
            MessageSenderImpl.getInstance().sendKafkaRecord(Constant.BLOCK_TRIGGER, "block", trigger);
        }
    }
}
