package org.ignitionmdc.apache.kafka;

import com.inductiveautomation.ignition.client.gateway_interface.ModuleRPCFactory;
import com.inductiveautomation.ignition.common.BundleUtil;
import com.inductiveautomation.ignition.common.script.builtin.KeywordArgs;
import com.inductiveautomation.ignition.common.script.hints.ScriptArg;
import com.inductiveautomation.ignition.common.script.hints.ScriptFunction;
import org.python.core.PyList;
import org.python.core.PyObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Kafka_Com {

    static {
        BundleUtil.get().addBundle(
                Kafka_Com.class.getSimpleName(),
                Kafka_Com.class.getClassLoader(),
                Kafka_Com.class.getName().replace('.', '/')
        );
    }

    @ScriptFunction(docBundlePrefix = "Kafka_Com")
    public static List<Map> getConsumer(@ScriptArg("kafkaConnectionName") String kafkaConnectionName,
                                        @ScriptArg("address") String address,
                                        @ScriptArg("topic") String topic,
                                        @ScriptArg("groupname") String groupname) {
        KafkaRPC rpc = ModuleRPCFactory.create("org.ignitionmdc.apache.kafka.kafka", KafkaRPC.class);
        return rpc.RPCGetConsumer(kafkaConnectionName, address,topic,groupname);
    }

//    public static List<Map> getSSLConsumer(String kafkaConnectionName, String address, String topic, String groupname) {
//        KafkaRPC rpc = ModuleRPCFactory.create("org.ignitionmdc.apache.kafka.kafka", KafkaRPC.class);
//        return rpc.RPCGetSSLConsumer(kafkaConnectionName, address,topic,groupname);
//    }

    @ScriptFunction(docBundlePrefix = "Kafka_Com")
    @KeywordArgs(names = {"kafkaConnectionName", "address", "topic", "partition", "timestamp", "recordKey", "recordValue", "headerKeys", "headerValues"},
            types = {String.class, String.class, String.class, Integer.class, Long.class, String.class, String.class, PyList.class, PyList.class})
    public static HashMap publish(PyObject[] pyObjects, String[] keywords) throws Exception{
        KafkaRPC rpc = ModuleRPCFactory.create("org.ignitionmdc.apache.kafka.kafka", KafkaRPC.class);
        return rpc.RPCPublish(pyObjects, keywords);
    }

    @ScriptFunction(docBundlePrefix = "Kafka_Com")
    public static List<Map> seek(@ScriptArg("kafkaConnectionName") String kafkaConnectionName,
                                 @ScriptArg("address") String address,
                                 @ScriptArg("topic") String topic,
                                 @ScriptArg("groupname") String groupname,
                                 @ScriptArg("partition") int partition,
                                 @ScriptArg("numOfMsgs") long numOfMsgs){
        KafkaRPC rpc = ModuleRPCFactory.create("org.ignitionmdc.apache.kafka.kafka", KafkaRPC.class);
        return rpc.RPCSeek(kafkaConnectionName, address, topic, groupname, partition, numOfMsgs);
    }

    @ScriptFunction(docBundlePrefix = "Kafka_Com")
    public static List<Integer> getTopicPartitions(@ScriptArg("kafkaConnectionName") String kafkaConnectionName,
                                                     @ScriptArg("address") String address,
                                                     @ScriptArg("topic") String topic,
                                                     @ScriptArg("groupname") String groupname){
        KafkaRPC rpc = ModuleRPCFactory.create("org.ignitionmdc.apache.kafka.kafka", KafkaRPC.class);
        return rpc.RPCGetTopicPartitions(kafkaConnectionName, address, topic, groupname);
    }
}
