/*
 * Copyright (C) 2012-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tron.eventplugin.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.pf4j.CompoundPluginDescriptorFinder;
import org.pf4j.ManifestPluginDescriptorFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.pf4j.DefaultPluginManager;
import org.pf4j.PluginManager;
import org.tron.common.logsfilter.IPluginEventListener;
import org.tron.common.logsfilter.trigger.BlockLogTrigger;
import org.tron.common.logsfilter.trigger.Trigger;

import java.io.File;
import java.util.List;
import java.util.Objects;

public class PluginLauncher {
    private static final Logger logger = LoggerFactory.getLogger(PluginLauncher.class);

    public static void main(String[] args) {
        String path = "/home/java-tron/plugin-kafka-1.0.0.zip";

        File dir = new File(path);
        // create the plugin manager
        final PluginManager pluginManager = new DefaultPluginManager(dir.toPath()) {
          @Override
          protected CompoundPluginDescriptorFinder createPluginDescriptorFinder() {
            return new CompoundPluginDescriptorFinder()
                .add(new ManifestPluginDescriptorFinder());
          }
        };

        File file = new File(path);

        pluginManager.loadPlugin(file.toPath());
        pluginManager.startPlugins();

        List<IPluginEventListener> eventListeners;
        eventListeners = pluginManager.getExtensions(IPluginEventListener.class);

        if (Objects.isNull(eventListeners)) return;

        eventListeners.forEach(listener -> {
            listener.setServerAddress("127.0.0.1:9092");
        });

        eventListeners.forEach(listener -> {
            listener.setTopic(Trigger.BLOCK_TRIGGER, "block");
            listener.setTopic(Trigger.TRANSACTION_TRIGGER, "transaction");
            listener.setTopic(Trigger.CONTRACTEVENT_TRIGGER, "contractevent");
            listener.setTopic(Trigger.CONTRACTLOG_TRIGGER, "contractlog");
        });

        ObjectMapper objectMapper = new ObjectMapper();
        for (int index = 0; index < 666; ++index){
            BlockLogTrigger trigger = new BlockLogTrigger();
            trigger.setBlockNumber(index);
            trigger.setBlockHash("000000000002f5834df6036318999576bfa23ff1a57e0538fa87d5a90319659e");
            trigger.setTimeStamp(System.currentTimeMillis());
            trigger.setTransactionSize(100);

            eventListeners.forEach(listener -> {
                try {
                    listener.handleBlockEvent(objectMapper.writeValueAsString(trigger));
                } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                    e.printStackTrace();
                }
            });
        }

        while (true){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //pluginManager.stopPlugins();
    }
}
