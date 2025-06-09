package org.ignitionmdc.apache.kafka.config;

import com.inductiveautomation.ignition.gateway.localdb.persistence.RecordMeta;
import com.inductiveautomation.ignition.gateway.web.components.RecordActionTable;
import com.inductiveautomation.ignition.gateway.web.pages.IConfigPage;

public class KafkaRecordTable extends RecordActionTable {

    public static final String[] PATH = {"kafka", "connections"};

    public KafkaRecordTable(IConfigPage iConfigPage){
        super(iConfigPage);
    }

    @Override
    protected RecordMeta<KafkaConnectionSettings> getRecordMeta() {
        return KafkaConnectionSettings.META;
    }

    @Override
    public String[] getMenuPath(){
        return PATH;
    }
}
