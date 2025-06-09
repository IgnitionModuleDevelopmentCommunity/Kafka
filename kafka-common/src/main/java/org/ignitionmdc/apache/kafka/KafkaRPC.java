package org.ignitionmdc.apache.kafka;
import org.python.core.PyObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface KafkaRPC {
//    List<Map> RPCGetSSLConsumer(String kafkaConnectionName, String address, String topic, String groupname);
    List<Map> RPCGetConsumer(String kafkaConnectionName, String address, String topic, String groupname);
    HashMap RPCPublish(PyObject[] pyObjects, String[] keywords) throws Exception;
    List<Map> RPCSeek(String conn, String addr, String topic, String group, int partition, long numOfMsgs);
    List<Integer> RPCGetTopicPartitions(String kafkaConfigName, String address, String topic, String groupname);
}
