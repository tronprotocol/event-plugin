package org.tron.eventplugin;

import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;

public class MongodbLogFilterPlugin extends Plugin {

    public MongodbLogFilterPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
        MongodbSenderImpl.getInstance().close();
    }
}
