package org.ignitionmdc.apache.kafka;

import com.inductiveautomation.ignition.common.script.hints.ScriptFunctionDocProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class KafkaDocs implements ScriptFunctionDocProvider {
    private static Logger logger = LoggerFactory.getLogger("Kafka Docs");

    @Override
    public String getMethodDescription(String path, Method method) {
        logger.info("getMethodDescription called.  path: '"+path+"', method:'"+method+"'");
        if (method.getName().equals("getConsumer")) {return "Creates a server connection and polls the Kafka server for recent messages.";}
        if (method.getName().equals("getSSLConsumer")) {return "Creates an encrypted server connection and polls the Kafka server for recent messages.";}
        if (method.getName().equals("createProducer")) {return "Creates a kafka producer object";}
        if (method.getName().equals("createSSLProducer")) {return "Creates an encrypted kafka producer object";}
        if (method.getName().equals("transmitData")) {return "Creates producer record and calls the send method of the kafka producer object";}
        return "";
    }

    @Override
    public Map<String, String> getParameterDescriptions(String path, Method method) {
        HashMap<String, String> map = new HashMap<>();
        if (method.getName().equals("getConsumer") || method.getName().equals("getSSLConsumer")) {
            map.put("address", "Kafka server address, including port");
            map.put("topic", "Kafka topic");
            map.put("group", "Kafka group id");
        }
        if(method.getName().equals("createProducer") || method.getName().equals("createSSLProducer")){
            map.put("address", "Kafka server address, including port");
        }
        if(method.getName().equals("transmitData")){
            map.put("producerKey", "The key to a kafka producer object");
            map.put("topic", "Kafka topic");
            map.put("data", "The data to be transmitted to kafka by the producer");
        }
        return map;
    }

    @Override
    public String getReturnValueDescription(String path, Method method) {
        if (method.getName().equals("getConsumer") || method.getName().equals("getSSLConsumer")){
            return "A list of message objects.  Each message object contains keys for 'value', 'timestamp', 'partition', 'offset', and 'key'.";
        }
        if(method.getName().equals("createProducer") || method.getName().equals("createSSLProducer")){
            return "The key to access a kafka producer object.";
        }
        if (method.getName().equals("transmitData")){
            return "boolean true if successful.";
        }
        return "";
    }
}
