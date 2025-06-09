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
        return "";
    }

    @Override
    public Map<String, String> getParameterDescriptions(String path, Method method) {
        HashMap<String, String> map = new HashMap<String, String>();
        if (method.getName().equals("getConsumer") || method.getName().equals("getSSLConsumer")) {
            map.put("address", "Kafka server address, including port");
            map.put("topic", "Kafka topic");
            map.put("group", "Kafka group id");
        }
        return map;
    }

    @Override
    public String getReturnValueDescription(String path, Method method) {
        return "A list of message objects.  Each message object contains keys for 'value', 'timestamp', 'partition', 'offset', and 'key'.";
    }
}
