package org.ignitionmdc.apache.kafka;

//import com.inductiveautomation.ignition.gateway.SRContext;

import com.inductiveautomation.ignition.common.script.builtin.KeywordArgs;
import com.inductiveautomation.ignition.common.script.builtin.PyArgumentMap;
import com.inductiveautomation.ignition.gateway.localdb.persistence.IRecordListener;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;
import com.inductiveautomation.ignition.gateway.web.models.KeyValue;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.config.ConfigException;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.ignitionmdc.apache.kafka.config.KafkaConnectionSettings;
import org.python.core.Py;
import org.python.core.PyDictionary;
import org.python.core.PyList;
import org.python.core.PyObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import simpleorm.dataset.SQuery;

import java.io.File;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class Kafka implements KafkaRPC {
    private static GatewayContext context;
    private static Logger logger = LoggerFactory.getLogger("Kafka Module");


    private IRecordListener<KafkaConnectionSettings> recordListener = new IRecordListener<KafkaConnectionSettings>() {
        @Override
        public void recordUpdated(KafkaConnectionSettings settings) {
            logger.info(String.format("A %s setting was changed", settings.getName()));
            for (Map.Entry<String, Consumer> entry : GatewayHook.ConsumerHashMap.entrySet()) {
                String[] key = entry.getKey().split("-");
                // only update those with the same Kafka config setting name
                if (settings.getName().equals(key[0])) {

                    Consumer c = entry.getValue();
                    synchronized (c){
                        c.close();
                    }
                    c = getConsumerObj(key[0], key[1], key[2], key[3]);
                    entry.setValue(c);
                    logger.info(String.format("Updated consumer properties for %s", settings.getName()));
                }
            }

            for (Map.Entry<String, Producer> stringProducerEntry : GatewayHook.ProducerHashMap.entrySet()) {
                String[] key = stringProducerEntry.getKey().split("-");
                if (settings.getName().equals(key[0])) {
                    Producer producer = stringProducerEntry.getValue();

                    synchronized (producer) {
                        producer.close();
                    }
                    producer = getProducer(key[0], key[1], key[2]);
                    stringProducerEntry.setValue(producer);
                    logger.info(String.format("Updated producer properties for %s", settings.getName()));
                }
            }
        }

        @Override
        public void recordAdded(KafkaConnectionSettings settings) {
        }

        @Override
        public void recordDeleted(KeyValue keyValue) {

        }
    };

    public Kafka(GatewayContext _context) {
/*        if (_context == null) {
            this.context = _context;
        }
        else {
            this.context = SRContext.get();
        }*/
        this.context = _context;
    }

    // only used to add a listener
    public void init() {
        if (!Arrays.asList(KafkaConnectionSettings.META.getRecordListeners()).contains(recordListener)) {
            logger.info("Adding listener");
            KafkaConnectionSettings.META.addRecordListener(recordListener);
        }
    }

    public void shutdown() {
        logger.info("Removing listener");
        KafkaConnectionSettings.META.removeRecordListener(recordListener);
    }

//    public static Consumer<String, String> getSSLConsumerObj(String kafkaConfigName, String address, String topic, String groupname) {
//        logger.debug("Creating a new ssl consumer object ");
//        // Create the consumer using props.
//        Properties props = buildConsumerProps(kafkaConfigName, address,groupname,true);
//        Thread.currentThread().setContextClassLoader(null);
//        final Consumer<String, String> consumer = new KafkaConsumer<String, String>(props);
//
//        // Do this to test the validity of the connection, error gets thrown if server config is no good
//        // No timeout property exists of 2019-05-13 for a timeout to be caught in this situation
//        // TimeoutException will be thrown after the period defined
//        consumer.listTopics(Duration.ofSeconds(10));
//
//        // Subscribe to the topic.
//        consumer.subscribe(Collections.singletonList(topic));
//
//        // An alternate way to set up a consumer to listen to the given topic
////        TopicPartition tp = new TopicPartition(topic, 0);
////        List<TopicPartition> tps = Arrays.asList(tp);
////        consumer.assign(tps);
//
//        return consumer;
//    }

    public static Consumer<String, String> getConsumerObj(String kafkaConfigName, String address, String topic, String groupname) {
        logger.debug("Creating a new non-ssl consumer object ");
        // Create the consumer using props.
        Thread.currentThread().setContextClassLoader(null);
        Properties props = buildConsumerProps(kafkaConfigName, address,groupname);
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

    private static Properties buildConsumerProps(String kafkaConfigName, String address, String groupname){
        String homePath = getGatewayHome();
        String sep = File.separator;

        // will return a null if no kafka settings
        KafkaConnectionSettings settings = getSettings(kafkaConfigName);
        Properties props = new Properties();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, address);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupname);
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 3000);
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 10000);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.put(ConsumerConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, 60000);
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 300000);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, 1000);

        // load and put calls both result in the Properties obj placing key/value pairs in a hashmap; we'll load with our
        // preset properties first, then they can override
        if (settings.getConsumerConfig() != "" && settings.getConsumerConfig() != null) {
            try {
                props.load(new StringReader(settings.getConsumerConfig().replace(" ", "\n")));
            } catch (Exception e) {
                logger.error("An error occurred while loading consumer properties, will try to continue with previous settings", e);
            }
        }
        
        if (settings != null && settings.isSslEnabled()){
            logger.debug("Let's use SSL");
            logger.debug("homepath = " + homePath);
            props.put(SslConfigs.SSL_PROTOCOL_CONFIG,"SSL");
            props.put("security.protocol","SSL");
            props.put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG,"");

//            String keystorePath = homePath+String.format("%sdata%scertificates%skafka.ssl.key",sep,sep,sep);
            // Keystore settings
            logger.debug("SSL Keystore Path: "+ settings.getKeystore());

            props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG,settings.getKeystore());
            props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG,settings.getKeystorePass());
            props.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG,settings.getKeystorePass());
            
            // Truststore settings
//            String truststorePath = homePath+String.format("%sdata%scertificates%struststore.jks",sep,sep,sep);
            logger.debug("Trust Store Path: " + settings.getTrustStore());
            if (fileExists(settings.getTrustStore())) {
                logger.debug("Trust Store does exist");
                props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, settings.getTrustStore());
                props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, settings.getKeystorePass());
            }
        }

        return props;
    }
//    public List<Map> getSSLConsumer(String kafkaConfigName, String address, String topic, String groupname){
//        try {
//            return RPCGetSSLConsumer(kafkaConfigName, address, topic, groupname);
//        } catch (Exception e) {
//            logger.error("Error in getSSLConsumer", e);
//            return null;
//        }
//
//    }
    public List<Map> getConsumer(String kafkaConfigName, String address, String topic, String groupname){
        try {
            return RPCGetConsumer(kafkaConfigName, address, topic, groupname);
        } catch (IllegalStateException illegalStateException) {
            logger.error("The consumer is in an unexpected state, check the configuration settings for invalid settings", illegalStateException);
            return null;
        } catch (Exception e) {
            logger.error("Unexpected error in getConsumer", e);
            return null;
        }
    }
//    public List<Map> RPCGetSSLConsumer(String kafkaConfigName, String address, String topic, String groupname){
//
//        String keyName = String.format("%s-%s-%s",address,topic,groupname);
//
//        Consumer cObj = null;
//
//        if (GatewayHook.ConsumerHashMap.containsKey(keyName)){
//            cObj = GatewayHook.ConsumerHashMap.get(keyName);
//        }
//        else{
//            cObj = getSSLConsumerObj(kafkaConfigName, address,topic,groupname);
//            GatewayHook.ConsumerHashMap.put(keyName, cObj);
//        }
//        return makeHashList(cObj);
//    }

/*
    We always pass in the same data
 */
    public List<Map> RPCGetConsumer(String kafkaConfigName, String address, String topic, String groupname){

        Consumer cObj = getOrCreateConsumer(kafkaConfigName, address, topic, groupname);
        // synchronize here and in the listener, so that the same object is held by only one thread
        // at a time
        synchronized (cObj) {
            ConsumerRecords recs = cObj.poll(Duration.ofSeconds((5)));
            List<Map> toReturn = makeHashList(recs);
            // only commit after all possible processing done; could still fail in transit
            cObj.commitSync();
            return toReturn;
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
    private List<Map> makeHashList(ConsumerRecords recs) {
        List<Map> dict = new ArrayList();

        Iterator <ConsumerRecord<String,String>>iter = recs.iterator();
        //System.out.println(iter);
        while (iter.hasNext()) {
            HashMap hMap = new HashMap();
            ConsumerRecord cr = iter.next();
//
            HashMap hm = new HashMap();
            for (Header h:cr.headers().toArray()){
                hm.put(h.key(), new String(h.value()));
            }

            hMap.put("headers", hm);
            hMap.put("offset",    cr.offset());
            hMap.put("key",       cr.key());
            hMap.put("value",     cr.value());
            hMap.put("timestamp", cr.timestamp());
            hMap.put("partition", cr.partition());
            dict.add(hMap);
        }
        return dict;
    }

    private static KafkaConnectionSettings getSettings(String recordName){

        if (recordName == null){
            KafkaConnectionSettings settings = context.getPersistenceInterface().queryOne(new SQuery<>(KafkaConnectionSettings.META));

            if (settings == null){
                logger.error("No Kafka settings found");
                return null;
            } else {
                return settings;
            }

        } else {
            KafkaConnectionSettings settings = context.getPersistenceInterface().queryOne(new SQuery<>(KafkaConnectionSettings.META)
            .eq(KafkaConnectionSettings.Name, recordName));

            if (settings == null){
                // nothing by that name, find anything
                return getSettings(null);
            } else {
                return settings;
            }
        }
    }

    public HashMap publish(PyObject[] pyObjects, String[] keywords) throws Exception{
        try{
            return RPCPublish(pyObjects, keywords);
        } catch (Exception e){
            throw new Exception(e);
        }
    }

    public List<Map> seek(String conn, String addr, String topic, String group, int partition, long numOfMsgs){
        return RPCSeek(conn, addr, topic, group, partition, numOfMsgs);
    }

    public List<Map> RPCSeek(String conn, String addr, String topic, String group, int partition, long numOfMsgs){
        String keyName = String.format("%s-%s-%s",addr,topic,group);

        Consumer consumer = getOrCreateConsumer(conn, addr, topic, group);

        // 1st poll ensures that partition is assigned
        consumer.poll(Duration.ofSeconds((5)));
        TopicPartition tp = new TopicPartition(topic, partition);
        long currentOffset = consumer.position(tp);
        if (currentOffset < numOfMsgs) {
            consumer.seek(tp, new OffsetAndMetadata(0, keyName));
        } else {
            consumer.seek(tp, new OffsetAndMetadata(currentOffset-numOfMsgs, keyName));
        }

        long newOffset = consumer.position(tp);
        ConsumerRecords consumerRecords = consumer.poll(Duration.ofSeconds((5)));
        long afterOffset = consumer.position(tp);
        logger.debug(String.format("key: %s, currentoffset: %s, newoffset: %s, afteroffset: %s", keyName, currentOffset, newOffset, afterOffset));
//        ConsumerRecords recs = c.poll(Duration.ofSeconds((5)));
//        logger.info(String.format("position: %s", c.position(new TopicPartition("eden-test", 0))));

        List<Map> toReturn = makeHashList(consumerRecords);
        consumer.commitSync();
        return toReturn;

    }



    @Override
    @KeywordArgs(names = {"kafkaConnectionName", "address", "topic", "partition", "timestamp", "key", "value", "headerKeys", "headerValues"},
            types = {String.class, String.class, String.class, Integer.class, Long.class, String.class, String.class, PyList.class, PyList.class})
    public HashMap RPCPublish(PyObject[] pyObjects, String[] keywords) throws Exception{

        PyArgumentMap args = PyArgumentMap.interpretPyArgs(pyObjects, keywords, Kafka.class, "RPCPublish");

        String kafkaConnectionName = args.getStringArg("kafkaConnectionName");
        String address = args.getStringArg("address");
        String topic = args.getStringArg("topic");
        Integer partition = args.getIntArg("partition");
        Long timestamp = args.getLongArg("timestamp");
        String key = args.getStringArg("key");
        String value = args.getStringArg("value");

        List<Object> headerKeys = getListFromPython((PyList) args.getArg("headerKeys"));
        List<String> convertedHeaderKeys = headerKeys.stream().map(object -> Objects.toString(object))
                .collect(Collectors.toList());

        List<Object> headerVals = getListFromPython((PyList) args.getArg("headerValues"));
        List<String> convertedHeaderVals = headerVals.stream().map(object -> Objects.toString(object))
                .collect(Collectors.toList());


        logger.debug(String.format("args: %s, %s, %s, %s, %s, %s, %s, %s, %s", kafkaConnectionName, address, topic, partition, timestamp, key, value, convertedHeaderKeys, convertedHeaderVals));

        // allow any exceptions to reach the caller
        try {
            Producer<String, String> producer = getOrCreateProducer(kafkaConnectionName, address, topic);
            ProducerRecord<String, String> pr = new ProducerRecord<>(topic, partition, timestamp, key, value);

            for (int i = 0; i < convertedHeaderKeys.size(); i++) {
                pr.headers().add(convertedHeaderKeys.get(i), convertedHeaderVals.get(i).getBytes(StandardCharsets.UTF_8));
            }

            RecordMetadata meta =  producer.send(pr).get();
            HashMap hmap = new HashMap<>();
            hmap.put("offset", meta.offset());
            hmap.put("timestamp", meta.timestamp());
            hmap.put("partition", meta.partition());
            hmap.put("topic", meta.topic());

            return hmap;

        } catch (Exception e) {
            throw new Exception(e);
        }

    }

    private static Properties buildProducerProps(String kafkaConfigName, String address){
        Thread.currentThread().setContextClassLoader(null);

        String sep = File.separator;

        // will return a null if no kafka settings
        KafkaConnectionSettings settings = getSettings(kafkaConfigName);

        Properties props = new Properties();
        //in the event of multiple producers
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "ignition");

        // default batch size is 16384
        // default buffer mem is 33554432
        // default linger is 0
        // default retries is Integer.MAX_VALUE
        // default acks is 1(one), while -1(all) is more stringent

        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, address);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 0);
        props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 15000);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // load and put calls both result in the Properties obj placing key/value pairs in a hashmap; we'll load with our
        // preset properties first, then they can override
        if (settings.getProducerConfig() != "" && settings.getProducerConfig() != null) {
            try {
                props.load(new StringReader(settings.getProducerConfig().replace(" ", "\n")));
            } catch (Exception e) {
                logger.error("An error occurred while loading producer properties, will try to continue with presets", e);
            }
        }

        if (settings != null && settings.isSslEnabled()){
            logger.debug("SSL enabled for producer config");
            props.put(SslConfigs.SSL_PROTOCOL_CONFIG,"SSL");
            props.put("security.protocol","SSL");
            props.put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG,"");

            logger.debug("SSL Keystore Path: "+ settings.getKeystore());
            props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG,settings.getKeystore());
            props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG,settings.getKeystorePass());
            props.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG,settings.getKeystorePass());

            logger.debug("Trust Store Path: " + settings.getTrustStore());
            if (fileExists(settings.getTrustStore())) {
                logger.debug("Trust Store does exist");
                props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, settings.getTrustStore());
                props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, settings.getKeystorePass());
            }
        }

        return props;
    }

    private static Producer<String, String> getProducer(String kafkaConnectionName, String address, String topic){
        Properties props = buildProducerProps(kafkaConnectionName, address);
        return new KafkaProducer<String, String>(props);

    }

    private static List<Object> getListFromPython(PyList pyList) {
        if (pyList == null){
            return null;
        }
        PyObject[] underlyingArray = pyList.getArray();

        ArrayList<Object> convertedList = new ArrayList<>();

        for(int i = 0; i < underlyingArray.length; i++) {
            if (underlyingArray[i] instanceof PyList) {
                convertedList.add(getListFromPython((PyList)underlyingArray[i]));
            } else {
                convertedList.add(Py.tojava(underlyingArray[i], Object.class));
            }
        }

        return convertedList;
    }

    private static Consumer getOrCreateConsumer(String kafkaConfigName, String address, String topic, String groupname) {
        // need the connection name to be part of the key because of the Properties associated with a KafkaConsumer,
        // i.e. without connection name, conn1 and conn2 would pull the same consumer, but that consumer may have
        // been instantiated with Properties in conn1, not conn2
        String keyName = String.format("%s-%s-%s-%s", kafkaConfigName, address,topic,groupname);
        Consumer cObj = null;
        if (GatewayHook.ConsumerHashMap.containsKey(keyName)){
            cObj = GatewayHook.ConsumerHashMap.get(keyName);
        }
        else{
            cObj = getConsumerObj(kafkaConfigName, address,topic,groupname);
            GatewayHook.ConsumerHashMap.put(keyName, cObj);
        }

        return cObj;
    }
    private static Producer getOrCreateProducer(String kafkaConfigName, String address, String topic) {
        // need the connection name to be part of the key because of the Properties associated with a Producer
        String keyName = String.format("%s-%s-%s", kafkaConfigName, address,topic);
        Producer producer = null;
        if (GatewayHook.ProducerHashMap.containsKey(keyName)){
            producer = GatewayHook.ProducerHashMap.get(keyName);
        }
        else{
            producer = getProducer(kafkaConfigName, address,topic);
            GatewayHook.ProducerHashMap.put(keyName, producer);
        }

        return producer;
    }

    public List<Integer> getTopicPartitions(String kafkaConfigName, String address, String topic, String groupname){
        return RPCGetTopicPartitions(kafkaConfigName, address, topic, groupname);
    }

    /**
     * After consideration, getOrCreateConsumer wasn't the best choice to grab an instance of Consumer. To limit accidental
     * topic creation (getConsumerObj can create a new topic if the topic is brand new), we create an instance of a Consumer
     * but don't subscribe it, nor do we cache it
     */
    public List<Integer> RPCGetTopicPartitions(String kafkaConfigName, String address, String topic, String groupname) {

        ArrayList<Integer> toRet = new ArrayList<>();
        Thread.currentThread().setContextClassLoader(null);
        Properties props = buildConsumerProps(kafkaConfigName, address,groupname);
        Consumer<String, String> consumer = new KafkaConsumer<String, String>(props);

        logger.debug(String.format("RPCGetConsumerPartitions: %s",consumer.listTopics(Duration.ofSeconds(10)).keySet()));

        if (consumer.listTopics().getOrDefault(topic, null) != null) {
            // if the topic doesn't exist in Kafka, partitionsFor will create it and a partition, so need a check
            // before calling it
            List<PartitionInfo> partitionInfos = consumer.partitionsFor(topic);
            for (PartitionInfo info : partitionInfos) {
                toRet.add(info.partition());
            }
            logger.debug(String.format("RPCGetConsumerPartition: %s", consumer.partitionsFor(topic)));
            Collections.sort(toRet);
            return toRet;

        } else {
            logger.info(String.format("Non existent topic %s", topic));
            return toRet;
        }

    }
}
