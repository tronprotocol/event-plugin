package org.tron.eventplugin.serializer;

import com.alibaba.fastjson.JSON;
import org.apache.kafka.common.serialization.Serializer;
import org.tron.common.logsfilter.trigger.ContractLogTrigger;

import java.util.Map;

public class ContractLogSerializer implements Serializer<ContractLogTrigger> {

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        // default implementation
    }

    @Override
    public byte[] serialize(String topic, ContractLogTrigger data) {
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
