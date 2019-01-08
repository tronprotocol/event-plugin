package org.tron.eventplugin;

import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tron.common.logsfilter.IPluginEventListener;
import org.tron.mongodb.MongoConfig;
import org.tron.mongodb.MongoManager;
import org.tron.mongodb.MongoTemplate;

import java.util.Map;
import java.util.Objects;

@Extension
public class MongodbEventListener implements IPluginEventListener {

    private static final Logger log = LoggerFactory.getLogger(MongodbEventListener.class);

    @Override
    public void setServerAddress(String address) {

        if (Objects.isNull(address) || address.length() == 0){
            return;
        }

        MongodbSenderImpl.getInstance().setServerAddress(address);

        // MessageSenderImpl should never init until server address is set
        MongodbSenderImpl.getInstance().init();
    }

    @Override
    public void setTopic(int eventType, String topic) {
        MongodbSenderImpl.getInstance().setTopic(eventType, topic);
    }

    @Override
    public void handleBlockEvent(Object data) {

        if (Objects.isNull(data)){
            return;
        }

        MongodbSenderImpl.getInstance().getTriggerQueue().offer((String)data);
    }

    @Override
    public void handleTransactionTrigger(Object data) {
        if (Objects.isNull(data)){
            return;
        }

        MongodbSenderImpl.getInstance().getTriggerQueue().offer((String)data);
    }

    @Override
    public void handleContractLogTrigger(Object data) {
        if (Objects.isNull(data)){
            return;
        }

        MongodbSenderImpl.getInstance().getTriggerQueue().offer((String)data);
    }

    @Override
    public void handleContractEventTrigger(Object data) {
        if (Objects.isNull(data)){
            return;
        }

        MongodbSenderImpl.getInstance().getTriggerQueue().offer((String)data);
    }
}
