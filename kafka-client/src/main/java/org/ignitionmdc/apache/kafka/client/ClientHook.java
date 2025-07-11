package org.ignitionmdc.apache.kafka.client;

import com.inductiveautomation.ignition.client.model.ClientContext;
import com.inductiveautomation.ignition.common.licensing.LicenseState;
import com.inductiveautomation.ignition.common.script.ScriptManager;
import com.inductiveautomation.ignition.common.script.hints.PropertiesFileDocProvider;
import com.inductiveautomation.vision.api.client.AbstractClientModuleHook;
import org.ignitionmdc.apache.kafka.Kafka_Com;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientHook extends AbstractClientModuleHook {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void startup(ClientContext context, LicenseState activationState) throws Exception {
        super.startup(context, activationState);
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }

    @Override
    public void initializeScriptManager(ScriptManager manager) {
//        manager.addScriptModule("system.kafka", Kafka_Com.class, new PropertiesFileDocProvider());
        manager.addScriptModule("system.kafka", new Kafka_Com(), new PropertiesFileDocProvider());
    }
}
