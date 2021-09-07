package org.ignitionmdc.apache.kafka;

//import com.inductiveautomation.ignition.gateway.SRContext;

import com.inductiveautomation.ignition.gateway.model.GatewayContext;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.Duration;
import java.util.*;

public class Kafka implements KafkaRPC {
    private static GatewayContext context;
    private static Logger logger = LoggerFactory.getLogger("Kafka Module");

    public Kafka(GatewayContext _context) {
/*        if (_context == null) {
            this.context = _context;
        }
        else {
            this.context = SRContext.get();
        }*/
        this.context = _context;
    }

    public static Consumer<String, String> getSSLConsumerObj(String address, String topic, String groupname) {
        logger.debug("Creating a new ssl consumer object ");
        // Create the consumer using props.
        Properties props = buildConsumerProps(address,groupname,true);
        Thread.currentThread().setContextClassLoader(null);
        final Consumer<String, String> consumer = new KafkaConsumer<String, String>(props);

        // Do this to test the validity of the connection, error gets thrown if server config is no good
        // No timeout property exists of 2019-05-13 for a timeout to be caught in this situation
        // TimeoutException will be thrown after the period defined
        consumer.listTopics(Duration.ofSeconds(10));

        // Subscribe to the topic.
        consumer.subscribe(Collections.singletonList(topic));

        // An alternate way to set up a consumer to listen to the given topic
//        TopicPartition tp = new TopicPartition(topic, 0);
//        List<TopicPartition> tps = Arrays.asList(tp);
//        consumer.assign(tps);

        return consumer;
    }

    public static Consumer<String, String> getConsumerObj(String address, String topic, String groupname) {
        logger.debug("Creating a new non-ssl consumer object ");
        // Create the consumer using props.
        Thread.currentThread().setContextClassLoader(null);
        Properties props = buildConsumerProps(address,groupname,false);
        final Consumer<String, String> consumer = new KafkaConsumer<String, String>(props);

        // Do this to test the validity of the connection, error gets thrown if server config is no good
        // No timeout property exists of 2019-05-13 for a timeout to be caught in this situation
        // TimeoutException will be thrown after the period defined
        consumer.listTopics(Duration.ofSeconds(10));

        // Subscribe to the topic.
        consumer.subscribe(Collections.singletonList(topic));

        // An alternate way to set up a consumer to listen to the given topic
//        TopicPartition tp = new TopicPartition(topic, 0);
//        List<TopicPartition> tps = Arrays.asList(tp);
//        consumer.assign(tps);

        return consumer;
    }

    public static Producer<String, String> getProducerObj(String address){
        logger.debug("Creating a new non-ssl producer object");

        Thread.currentThread().setContextClassLoader(null);
        Properties props = buildProducerProps(address, false);
        final Producer<String, String> producer = new KafkaProducer<String, String>(props);

        return producer;
    }

    public static Producer<String, String> getSSLProducerObj(String address){
        logger.debug("Creating a new ssl encrypted producer object");

        Thread.currentThread().setContextClassLoader(null);
        Properties props = buildProducerProps(address, true);
        final Producer<String, String> producer = new KafkaProducer<String, String>(props);

        return producer;
    }

    private static Properties buildConsumerProps(String address, String groupname, boolean useSSL){

        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, address);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupname);
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 3000);
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 10000);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, 60000);
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 300000);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, 1000);

        if (useSSL){
            return getSSLProps(props);
        }
        else{
            return props;
        }
    }

    private static Properties buildProducerProps(String address, boolean useSSL){

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, address);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // safe producer settings
        props.setProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        props.setProperty(ProducerConfig.ACKS_CONFIG, "all");
        props.setProperty(ProducerConfig.RETRIES_CONFIG, Integer.toString(Integer.MAX_VALUE));
        props.setProperty(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "5");

        if (useSSL){
            return getSSLProps(props);
        }
        else{
            return props;
        }
    }

    private static Properties getSSLProps(Properties props){
        String homePath = getGatewayHome();
        String sep = File.separator;

        logger.debug("homepath = " + homePath);
        props.put(SslConfigs.SSL_PROTOCOL_CONFIG,"SSL");
        props.put("security.protocol","SSL");
        props.put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG,"");

        // Keystore settings
        logger.debug("SSL Keystore Path: "+ homePath+String.format("%swebserver%sssl.key",sep,sep,sep));

        props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG,homePath+String.format("%swebserver%sssl.key",sep,sep,sep));
        props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG,"ignition");
        props.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG,"ignition");

        // Truststore settings
        String truststorePath = homePath+String.format("%sdata%scertificates%struststore.jks",sep,sep,sep);
        if (fileExists(truststorePath)) {
            props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, truststorePath);
            props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, "ignition");
        }
        return props;
    }

    public List<Map> getSSLConsumer(String address, String topic, String groupname){
        return RPCGetSSLConsumer(address, topic, groupname);
    }
    public List<Map> getConsumer(String address, String topic, String groupname){
        return RPCGetConsumer(address, topic, groupname);
    }
    public String createProducer(String address){
        return RPCCreateProducer(address);
    }
    public String createSSLProducer(String address){
        return RPCCreateSSLProducer(address);
    }
    public boolean transmitData(String producerKey, String topic, String data){
        return RPCTransmitData(producerKey, topic, data);
    }

    public List<Map> RPCGetSSLConsumer(String address, String topic, String groupname){
        String keyName = String.format("%s-%s-%s",address,topic,groupname);

        Consumer cObj = null;

        if (GatewayHook.ConsumerHashMap.containsKey(keyName)){
            cObj = GatewayHook.ConsumerHashMap.get(keyName);
        }
        else{
            cObj = getSSLConsumerObj(address,topic,groupname);
            GatewayHook.ConsumerHashMap.put(keyName, cObj);
        }
        return makeHashList(cObj);
    }

    /*
        We always pass in the same data
     */
    public List<Map> RPCGetConsumer(String address, String topic, String groupname){
        String keyName = String.format("%s-%s-%s",address,topic,groupname);

        Consumer cObj = null;

        if (GatewayHook.ConsumerHashMap.containsKey(keyName)){
            cObj = GatewayHook.ConsumerHashMap.get(keyName);
        }
        else{
            cObj = getConsumerObj(address,topic,groupname);
            GatewayHook.ConsumerHashMap.put(keyName, cObj);
        }
        return makeHashList(cObj);
    }

    public String RPCCreateProducer(String address){
        String keyName = String.format("%s",address);

        if (!GatewayHook.ProducerHashMap.containsKey(keyName)) {
            Producer pObj = getProducerObj(address);
            GatewayHook.ProducerHashMap.put(keyName, pObj);
        }
        return(keyName);
    }

    public String RPCCreateSSLProducer(String address){
        String keyName = String.format("%s",address);

        if (!GatewayHook.ProducerHashMap.containsKey(keyName)) {
            Producer pObj = getSSLProducerObj(address);
            GatewayHook.ProducerHashMap.put(keyName, pObj);
        }
        return(keyName);
    }

    public boolean RPCTransmitData(String producerKey, String topic, String data){
        // Get the producer object from memory
        try{
            Producer pObj = GatewayHook.ProducerHashMap.get(producerKey);

            ProducerRecord<String, String> record =
                    new ProducerRecord<String, String>(topic, data);

            pObj.send(record);
            pObj.flush();
            return true;
        }
        catch(Exception e){
            return false;
        }
    }

    private static String getGatewayHome(){
        String absPath = context.getSystemManager().getDataDir().getAbsolutePath();
        return absPath.substring(0,absPath.lastIndexOf(File.separator));
    }

    private static boolean fileExists(String path){
        File fObj = new File(path);
        return fObj.exists();
    }

    /*
        This helper function decodes the Consumer object
        into a list of hashmaps
    */
    private List<Map> makeHashList(Consumer c) {
        List<Map> dict = new ArrayList();

        ConsumerRecords recs = c.poll(Duration.ofSeconds((5)));
        //c.commitSync();
        Iterator <ConsumerRecord<String,String>>iter = recs.iterator();
        //System.out.println(iter);
        while (iter.hasNext()) {
            HashMap hMap = new HashMap();
            ConsumerRecord cr = iter.next();
            //hMap.put("headers", cr.headers());    This needs to be properly serializable before putting into the hash map
            hMap.put("offset",    cr.offset());
            hMap.put("key",       cr.key());
            hMap.put("value",     cr.value());
            hMap.put("timestamp", cr.timestamp());
            hMap.put("partition", cr.partition());
            dict.add(hMap);
        }
        return dict;
    }
}
