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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.CompoundPluginDescriptorFinder;
import org.pf4j.DefaultPluginManager;
import org.pf4j.ManifestPluginDescriptorFinder;
import org.pf4j.PluginManager;
import org.tron.common.logsfilter.IPluginEventListener;
import org.tron.common.logsfilter.trigger.BlockLogTrigger;
import org.tron.common.logsfilter.trigger.EventTopic;

@Slf4j
public class PluginLauncher {

  public static void main(String[] args) {
    String path = "/Users/tron/sourcecode/eventplugin/build/plugins/plugin-mongodb-1.0.0.zip";

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

    log.info("start plugin...");
    if (Objects.isNull(eventListeners)) {
      return;
    }

    eventListeners.forEach(listener -> {
      listener.setServerAddress("127.0.0.1:27017");
    });

    eventListeners.forEach(listener -> {
      listener.setDBConfig("eventlog|tron|123456");
    });

    eventListeners.forEach(listener -> {
      listener.setTopic(EventTopic.BLOCK_TRIGGER.getType(), EventTopic.BLOCK_TRIGGER.getName());
      listener.setTopic(EventTopic.TRANSACTION_TRIGGER.getType(), EventTopic.TRANSACTION_TRIGGER.getName());
      listener.setTopic(EventTopic.CONTRACT_LOG_TRIGGER.getType(), EventTopic.CONTRACT_LOG_TRIGGER.getName());
      listener.setTopic(EventTopic.CONTRACT_EVENT_TRIGGER.getType(), EventTopic.CONTRACT_EVENT_TRIGGER.getName());
      listener.setTopic(EventTopic.SOLIDITY_TRIGGER.getType(), EventTopic.SOLIDITY_TRIGGER.getName());
      listener.setTopic(EventTopic.SOLIDITY_EVENT.getType(), EventTopic.SOLIDITY_EVENT.getName());
      listener.setTopic(EventTopic.SOLIDITY_LOG.getType(), EventTopic.SOLIDITY_LOG.getName());
    });

    eventListeners.forEach(IPluginEventListener::start);

    ObjectMapper objectMapper = new ObjectMapper();
    for (int index = 0; index < 2; ++index) {
      BlockLogTrigger trigger = new BlockLogTrigger();
      trigger.setBlockNumber(new Random().nextInt(10000)); //blockNumber is unique key
      trigger.setBlockHash("000000000002f5834df6036318999576bfa23ff1a57e0538fa87d5a90319659f");
      trigger.setTimeStamp(System.currentTimeMillis());
      trigger.setTransactionSize(100);

      String triggerData;
      try {
        triggerData = objectMapper.writeValueAsString(trigger);//convert to json
      } catch (JsonProcessingException e) {
        log.error("", e);
        continue;
      }
      eventListeners.forEach(listener -> {
        listener.handleBlockEvent(triggerData);
      });
    }

    try {
      Thread.sleep(10_000);
    } catch (InterruptedException e) {
      //ignore
    }
    log.info("try to close plugin...");

    // will invoke stop method of KafkaLogFilterPlugin or MongodbLogFilterPlugin
    pluginManager.stopPlugins();
  }
}
