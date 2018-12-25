package org.tron.eventplugin.serializer;

import com.alibaba.fastjson.JSON;
import org.apache.kafka.common.serialization.Serializer;
import org.tron.common.logsfilter.trigger.TransactionLogTrigger;

import java.util.Map;

public class TransactionLogSerializer implements Serializer<TransactionLogTrigger> {

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        // default implementation
    }

    @Override
    public byte[] serialize(String topic, TransactionLogTrigger data) {
        if (data == null){
            return new byte[0];
        }

        return JSON.toJSONBytes(data);
    }

    @Override
    public void close() {
        // default implementation
    }
}