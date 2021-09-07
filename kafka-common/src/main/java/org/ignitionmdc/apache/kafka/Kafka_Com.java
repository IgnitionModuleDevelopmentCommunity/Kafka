package org.ignitionmdc.apache.kafka;

import com.inductiveautomation.ignition.client.gateway_interface.ModuleRPCFactory;

import java.util.List;
import java.util.Map;


public class Kafka_Com {

    public static List<Map> getConsumer(String address, String topic, String groupname) {
        KafkaRPC rpc = ModuleRPCFactory.create("org.ignitionmdc.apache.kafka.kafka", KafkaRPC.class);
        return rpc.RPCGetConsumer(address,topic,groupname);
    }

    public static List<Map> getSSLConsumer(String address, String topic, String groupname) {
        KafkaRPC rpc = ModuleRPCFactory.create("org.ignitionmdc.apache.kafka.kafka", KafkaRPC.class);
        return rpc.RPCGetSSLConsumer(address,topic,groupname);
    }

    public static String createProducer(String address){
        KafkaRPC rpc = ModuleRPCFactory.create("org.ignitionmdc.apache.kafka.kafka", KafkaRPC.class);
        return rpc.RPCCreateProducer(address);
    }

    public static String createSSLProducer(String address){
        KafkaRPC rpc = ModuleRPCFactory.create("org.ignitionmdc.apache.kafka.kafka", KafkaRPC.class);
        return rpc.RPCCreateSSLProducer(address);
    }

    public static boolean transmitData(String producerKey, String topic, String data){
        KafkaRPC rpc = ModuleRPCFactory.create("org.ignitionmdc.apache.kafka.kafka", KafkaRPC.class);
        return rpc.RPCTransmitData(producerKey, topic, data);
    }
}
