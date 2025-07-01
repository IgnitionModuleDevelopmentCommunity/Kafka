package org.ignitionmdc.apache.kafka.config;

import com.inductiveautomation.ignition.gateway.localdb.persistence.*;
import com.inductiveautomation.ignition.gateway.web.components.editors.PasswordEditorSource;
import com.inductiveautomation.ignition.gateway.web.components.editors.SinglePasswordEditorSource;
import org.apache.wicket.validation.validator.UrlValidator;
import simpleorm.dataset.SFieldFlags;

public class KafkaConnectionSettings extends PersistentRecord {

    public static final RecordMeta<KafkaConnectionSettings> META = new RecordMeta<>(KafkaConnectionSettings.class, "KafkaConnectionSettings");

    public static IdentityField Id = new IdentityField(META);
    public static StringField Name = new StringField(META, "Name", SFieldFlags.SMANDATORY, SFieldFlags.SDESCRIPTIVE);

    public static StringField KEYSTORE_FIELD = new StringField(META, "SSL_Field");

    public static StringField KEYSTORE_PASSWORD = new StringField(META, "Keystore_Password");

    public static StringField TRUSTSTORE_FIELD = new StringField(META, "Truststore_Field");


    public static EncodedStringField TRUSTSTORE_PASSWORD = new EncodedStringField(META, "Truststore_Password");

    public static BooleanField SSL_ENABLED = new BooleanField(META, "SSL_Enabled", SFieldFlags.SMANDATORY, SFieldFlags.SDESCRIPTIVE);

    public static StringField CONSUMER_CONFIG = new StringField(META, "Consumer_Config", 8192);
    public static StringField PRODUCER_CONFIG = new StringField(META, "Producer_Config", 8192);

    static final Category Main = new Category("KafkaConnectionSettings.Category.Main", 1).include(Name);
    static final Category SSL = new Category("KafkaConnectionSettings.Category.SSL", 2).include(KEYSTORE_FIELD, KEYSTORE_PASSWORD, TRUSTSTORE_FIELD, TRUSTSTORE_PASSWORD, SSL_ENABLED);
    static final Category Config = new Category("KafkaConnectionSettings.Category.Config", 3).include(CONSUMER_CONFIG, PRODUCER_CONFIG);

    static{
        KEYSTORE_PASSWORD.getFormMeta().setEditorSource(PasswordEditorSource.getSharedInstance());
        TRUSTSTORE_PASSWORD.getFormMeta().setEditorSource(PasswordEditorSource.getSharedInstance());
    }

    @Override
    public RecordMeta<?> getMeta() {
        return META;
    }

    public String getName() {
        return getString(Name);
    }

    public String getKeystore(){
        return getString(KEYSTORE_FIELD);
    }

    public String getKeystorePass(){
        return getString(KEYSTORE_PASSWORD);
    }

    public String getTrustStore(){
        return getString(TRUSTSTORE_FIELD);
    }

    public String getTruststorePass(){
        return getString(TRUSTSTORE_PASSWORD);
    }

    public Boolean isSslEnabled(){
        return getBoolean(SSL_ENABLED);
    }

    public String getConsumerConfig() {
        return getString(CONSUMER_CONFIG);
    }

    public String getProducerConfig() {
        return getString(PRODUCER_CONFIG);
    }

}
