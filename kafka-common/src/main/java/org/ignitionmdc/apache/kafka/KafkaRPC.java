package org.ignitionmdc.apache.kafka;
import java.util.List;
import java.util.Map;

public interface KafkaRPC {
    List<Map> RPCGetSSLConsumer(String address, String topic, String groupname);
    List<Map> RPCGetConsumer(String address, String topic, String groupname);
    String RPCCreateProducer(String address);
    String RPCCreateSSLProducer(String address);
    boolean RPCTransmitData(String producerKey, String topic, String data);
}
