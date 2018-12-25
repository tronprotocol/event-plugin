package org.tron.common.logsfilter;

import org.pf4j.ExtensionPoint;

public interface IPluginEventListener extends ExtensionPoint {
    public void setServerAddress(String address);

    public void setTopic(int eventType, String topic);

    public void handleBlockEvent(Object data);

    public void handleTransactionTrigger(Object data);

    public void handleContractLogTrigger(Object data);

    public void handleContractEventTrigger(Object data);

}
