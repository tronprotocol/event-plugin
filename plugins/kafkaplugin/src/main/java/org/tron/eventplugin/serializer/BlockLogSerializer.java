package org.tron.eventplugin.serializer;

import com.alibaba.fastjson.JSON;
import org.apache.kafka.common.serialization.Serializer;
import org.tron.common.logsfilter.trigger.BlockLogTrigger;

import java.util.Map;

public class BlockLogSerializer implements Serializer<BlockLogTrigger> {

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        // default implementation
    }

    @Override
    public byte[] serialize(String topic, BlockLogTrigger data) {
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