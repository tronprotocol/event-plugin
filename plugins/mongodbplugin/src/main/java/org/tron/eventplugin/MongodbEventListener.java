package org.tron.eventplugin;

import java.util.Objects;
import org.pf4j.Extension;
import org.tron.common.logsfilter.IPluginEventListener;

@Extension
public class MongodbEventListener implements IPluginEventListener {

  @Override
  public void setServerAddress(String address) {
    if (Objects.isNull(address) || address.isEmpty()) {
      return;
    }
    MongodbSenderImpl.getInstance().setServerAddress(address);
  }

  @Override
  public void setTopic(int eventType, String topic) {
    MongodbSenderImpl.getInstance().setTopic(eventType, topic);
  }

  @Override
  public void setDBConfig(String dbConfig) {
    MongodbSenderImpl.getInstance().setDBConfig(dbConfig);
  }

  @Override
  public void start() {
    // MessageSenderImpl should never init until server address is set
    MongodbSenderImpl.getInstance().init();
  }

  @Override
  public void handleBlockEvent(Object data) {
    if (Objects.isNull(data)) {
      return;
    }
    MongodbSenderImpl.getInstance().getTriggerQueue().offer(data);
  }

  @Override
  public void handleTransactionTrigger(Object data) {
    if (Objects.isNull(data)) {
      return;
    }
    MongodbSenderImpl.getInstance().getTriggerQueue().offer(data);
  }

  @Override
  public void handleContractLogTrigger(Object data) {
    if (Objects.isNull(data)) {
      return;
    }
    MongodbSenderImpl.getInstance().getTriggerQueue().offer(data);
  }

  @Override
  public void handleContractEventTrigger(Object data) {
    if (Objects.isNull(data)) {
      return;
    }

    MongodbSenderImpl.getInstance().getTriggerQueue().offer(data);
  }

  @Override
  public void handleSolidityTrigger(Object data) {
    if (Objects.isNull(data)) {
      return;
    }
    MongodbSenderImpl.getInstance().getTriggerQueue().offer(data);
  }

  @Override
  public void handleSolidityLogTrigger(Object data) {
    if (Objects.isNull(data)) {
      return;
    }
    MongodbSenderImpl.getInstance().getTriggerQueue().offer(data);
  }

  @Override
  public void handleSolidityEventTrigger(Object data) {
    if (Objects.isNull(data)) {
      return;
    }
    MongodbSenderImpl.getInstance().getTriggerQueue().offer(data);
  }

  @Override
  public int getPendingSize() {
    return MongodbSenderImpl.getInstance().getTriggerQueue().size()
        + MongodbSenderImpl.getInstance().getQueue().size();
  }
}
