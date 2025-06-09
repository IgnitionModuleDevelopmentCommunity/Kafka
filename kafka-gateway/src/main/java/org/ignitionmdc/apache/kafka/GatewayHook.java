package org.ignitionmdc.apache.kafka;

import com.inductiveautomation.ignition.common.BundleUtil;
import com.inductiveautomation.ignition.common.licensing.LicenseState;
import com.inductiveautomation.ignition.common.script.ScriptManager;
import com.inductiveautomation.ignition.gateway.clientcomm.ClientReqSession;
import com.inductiveautomation.ignition.gateway.model.AbstractGatewayModuleHook;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;
import com.inductiveautomation.ignition.gateway.model.GatewayModuleHook;
import com.inductiveautomation.ignition.gateway.web.models.ConfigCategory;
import com.inductiveautomation.ignition.gateway.web.models.DefaultConfigTab;
import com.inductiveautomation.ignition.gateway.web.models.IConfigTab;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.producer.Producer;
import org.ignitionmdc.apache.kafka.config.KafkaConnectionSettings;
import org.ignitionmdc.apache.kafka.config.KafkaRecordTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.*;

public class GatewayHook extends AbstractGatewayModuleHook {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private GatewayContext context;
    public static Map<String, Consumer> ConsumerHashMap = new HashMap<>();
    public static Map<String, Producer> ProducerHashMap = new HashMap<>();
    public static final ConfigCategory KAFKA_CONFIG_CATEGORY = new ConfigCategory("kafka", "kafka.menu.root", 450);

    Kafka kafkaInst;
    public static final IConfigTab KAFKA_CONFIG_ENTRY = DefaultConfigTab.builder()
            .category(KAFKA_CONFIG_CATEGORY)
            .name("kafka_config")
            .i18n("kafka.menu.config")
            .page(KafkaRecordTable.class)
            .terms("kafka connections settings config")
            .build();

    @Override
    public void setup(GatewayContext gatewayContext) {
        this.context = gatewayContext;
         kafkaInst = new Kafka(context);
        try {
            context.getSchemaUpdater().updatePersistentRecords(KafkaConnectionSettings.META);
            BundleUtil.get().addBundle("kafka", getClass(), "Kafka");
            BundleUtil.get().addBundle("KafkaConnectionSettings", getClass(), "KafkaConnectionSettings");
        } catch (Exception e) {
            logger.error("Error setting up Kafka module", e);
        }
    }

    @Override
    public void startup(LicenseState licenseState) {

    }

    @Override
    public void shutdown() {
        BundleUtil.get().removeBundle("kafka");
        BundleUtil.get().removeBundle("KafkaConnectionSettings");
        kafkaInst.shutdown();
        kafkaInst = null;
        for (Map.Entry<String, Consumer> consumerEntry : GatewayHook.ConsumerHashMap.entrySet()) {
            Consumer consumer = consumerEntry.getValue();
            consumer.close();
        }

        ConsumerHashMap.clear();
        for (Map.Entry<String, Producer> stringProducerEntry : GatewayHook.ProducerHashMap.entrySet()) {
            Producer producer = stringProducerEntry.getValue();
            producer.close();
        }
        ProducerHashMap.clear();
    }

    @Override
    public void initializeScriptManager(ScriptManager manager) {
        kafkaInst.init();
        manager.addScriptModule("system.kafka", kafkaInst);
    }

    @Override
    public boolean isFreeModule() {
        return true;
    }

    @Override
    public boolean isMakerEditionCompatible() {
        return true;
    }

    @Override
    public Object getRPCHandler(ClientReqSession session, String projectName){
        return new Kafka(context);
    }

    @Override
    public List<ConfigCategory> getConfigCategories() {
        return Collections.singletonList(KAFKA_CONFIG_CATEGORY);
    }

    @Override
    public List<? extends IConfigTab> getConfigPanels() {
        return Arrays.asList(KAFKA_CONFIG_ENTRY);
    }
}
