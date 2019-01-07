package org.tron.eventplugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.tron.common.logsfilter.trigger.BlockLogTrigger;
import org.tron.common.logsfilter.trigger.ContractEventTrigger;
import org.tron.common.logsfilter.trigger.ContractLogTrigger;
import org.tron.common.logsfilter.trigger.TransactionLogTrigger;
import org.tron.orm.service.impl.EventLogServiceImpl;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import com.fasterxml.jackson.databind.ObjectMapper;

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

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private EventLogServiceImpl eventLogService;


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

        eventLogService = new EventLogServiceImpl();

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

    public void close() {
    }

    public BlockingQueue<Object> getTriggerQueue(){
        return triggerQueue;
    }

    public void handleBlockEvent(Object data) {
        if (blockTopic == null || blockTopic.length() == 0){
            return;
        }

        BlockLogTrigger trigger = new BlockLogTrigger();

        try {
            trigger = mapper.readValue((String)data, BlockLogTrigger.class);
        } catch (IOException e) {
            log.error("{}", e);
        }
        eventLogService.insertBlockTrigger(trigger);
    }

    public void handleTransactionTrigger(Object data) {
        if (Objects.isNull(data) || Objects.isNull(transactionTopic)){
            return;
        }

        TransactionLogTrigger trigger = new TransactionLogTrigger();

        try {
            trigger = mapper.readValue((String)data, TransactionLogTrigger.class);
        } catch (IOException e) {
            log.error("{}", e);
        }
        eventLogService.insertTransactionTrigger(trigger);

    }

    public void handleContractLogTrigger(Object data) {
        if (Objects.isNull(data) || Objects.isNull(contractLogTopic)){
            return;
        }

        ContractLogTrigger trigger = new ContractLogTrigger();

        try {
            trigger = mapper.readValue((String)data, ContractLogTrigger.class);
        } catch (IOException e) {
            log.error("{}", e);
        }

        eventLogService.insertContractLogTrigger(trigger);
    }

    public void handleContractEventTrigger(Object data) {
        if (Objects.isNull(data) || Objects.isNull(contractEventTopic)){
            return;
        }

        ContractEventTrigger trigger = new ContractEventTrigger();

        try {
            trigger = mapper.readValue((String)data, ContractEventTrigger.class);
        } catch (IOException e) {
            log.error("{}", e);
        }
        eventLogService.insertContractEventTrigger(trigger);
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
