package org.tron.eventplugin;

import java.util.Objects;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tron.common.logsfilter.IPluginEventListener;

@Extension
public class KafkaEventListener implements IPluginEventListener {

  private static final Logger log = LoggerFactory.getLogger(KafkaEventListener.class);

  @Override
  public void setServerAddress(String address) {
    if (Objects.isNull(address) || address.isEmpty()) {
      return;
    }
    KafkaSenderImpl.getInstance().setServerAddress(address);
  }

  @Override
  public void setTopic(int eventType, String topic) {
    KafkaSenderImpl.getInstance().setTopic(eventType, topic);
  }

  @Override
  public void setDBConfig(String dbConfig) {
    // empty implementation
  }

  @Override
  public void start() {
    // MessageSenderImpl should never init until server address is set
    KafkaSenderImpl.getInstance().init();
  }

  @Override
  public void handleBlockEvent(Object data) {
    if (Objects.isNull(data)) {
      return;
    }
    KafkaSenderImpl.getInstance().getTriggerQueue().offer(data);
  }

  @Override
  public void handleTransactionTrigger(Object data) {
    if (Objects.isNull(data)) {
      return;
    }
    KafkaSenderImpl.getInstance().getTriggerQueue().offer(data);
  }

  @Override
  public void handleSolidityTrigger(Object data) {
    if (Objects.isNull(data)) {
      return;
    }
    KafkaSenderImpl.getInstance().getTriggerQueue().offer(data);
  }

  @Override
  public void handleSolidityLogTrigger(Object data) {
    if (Objects.isNull(data)) {
      return;
    }
    KafkaSenderImpl.getInstance().getTriggerQueue().offer(data);
  }

  @Override
  public void handleSolidityEventTrigger(Object data) {
    if (Objects.isNull(data)) {
      return;
    }
    KafkaSenderImpl.getInstance().getTriggerQueue().offer(data);
  }

  @Override
  public int getPendingSize() {
    return KafkaSenderImpl.getInstance().getTriggerQueue().size();
  }

  @Override
  public void handleContractLogTrigger(Object data) {
    if (Objects.isNull(data)) {
      return;
    }
    KafkaSenderImpl.getInstance().getTriggerQueue().offer(data);
  }

  @Override
  public void handleContractEventTrigger(Object data) {
    if (Objects.isNull(data)) {
      return;
    }
    KafkaSenderImpl.getInstance().getTriggerQueue().offer(data);
  }
}
