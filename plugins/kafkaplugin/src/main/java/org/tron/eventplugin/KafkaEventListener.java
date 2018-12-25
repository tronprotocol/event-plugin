package org.tron.eventplugin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tron.common.logsfilter.IPluginEventListener;
import org.tron.common.logsfilter.trigger.BlockLogTrigger;
import org.tron.common.logsfilter.trigger.ContractEventTrigger;
import org.tron.common.logsfilter.trigger.ContractLogTrigger;
import org.tron.common.logsfilter.trigger.TransactionLogTrigger;

import java.io.IOException;
import java.util.Objects;

@Extension
public class KafkaEventListener implements IPluginEventListener {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventListener.class);

    private ObjectMapper mapper = new ObjectMapper();
    private String blockTopic = "";
    private String transactionTopic = "";
    private String contractEventTopic = "";
    private String contractLogTopic = "";

    @Override
    public void setServerAddress(String address) {

        if (Objects.isNull(address) || address.length() == 0){
            return;
        }

        MessageSenderImpl.getInstance().setServerAddress(address);

        // MessageSenderImpl should never init until server address is set
        MessageSenderImpl.getInstance().init();
    }

    @Override
    public void setTopic(int eventType, String topic) {
        if (eventType == Constant.BLOCK_TRIGGER){
            blockTopic = topic;
        }
        else if (eventType == Constant.TRANSACTION_TRIGGER){
            transactionTopic = topic;
        }
        else if (eventType == Constant.CONTRACTEVENT_TRIGGER){
            contractEventTopic = topic;
        }
        else if (eventType == Constant.CONTRACTLOG_TRIGGER){
            contractLogTopic = topic;
        }
    }

    @Override
    public void handleBlockEvent(Object data) {

        if (Objects.isNull(data) || Objects.isNull(blockTopic)){
            return;
        }

        BlockLogTrigger trigger = new BlockLogTrigger();

        try {
            trigger = mapper.readValue((String)data, BlockLogTrigger.class);
        } catch (IOException e) {
            log.error("{}", e);
        }

        MessageSenderImpl.getInstance().sendKafkaRecord(Constant.BLOCK_TRIGGER, blockTopic, trigger);
    }

    @Override
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

        MessageSenderImpl.getInstance().sendKafkaRecord(Constant.TRANSACTION_TRIGGER, transactionTopic, trigger);
    }

    @Override
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

        MessageSenderImpl.getInstance().sendKafkaRecord(Constant.CONTRACTLOG_TRIGGER, contractLogTopic, trigger);
    }

    @Override
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

        MessageSenderImpl.getInstance().sendKafkaRecord(Constant.CONTRACTEVENT_TRIGGER, contractEventTopic, trigger);
    }
}
