package org.tron.common.logsfilter;

import org.pf4j.ExtensionPoint;

public interface IPluginEventListener extends ExtensionPoint {
    void setServerAddress(String address);

    void setTopic(int eventType, String topic);

    void setDBConfig(String dbConfig);

    // start should be called after setServerAddress, setTopic, setDBConfig
    void start();

    void handleBlockEvent(Object data);

    void handleTransactionTrigger(Object data);

    void handleContractLogTrigger(Object data);

    void handleContractEventTrigger(Object data);

    void handleSolidityTrigger(Object trigger);

    void handleSolidityLogTrigger(Object trigger);

    void handleSolidityEventTrigger(Object trigger);

    int getPendingSize();
}
