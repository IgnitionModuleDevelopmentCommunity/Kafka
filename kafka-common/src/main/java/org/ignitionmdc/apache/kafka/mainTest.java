package org.ignitionmdc.apache.kafka;

import java.util.List;
import java.util.Map;
public class mainTest {
    public static void main(String[] args) {
        Kafka_Com inst = new Kafka_Com();

//        Consumer c = inst.getSSLConsumer("ia-ae-vuig8-209.ia.local:9093","newtopic2","SDK-Client122847");
        List<Map> c = inst.getConsumer(null, "ia-ae-vuig8-209.ia.local:9092","newtopic2","SDK-Client23978");
//        Consumer c = inst.getConsumer("ia-ae-vuig8-209.ia.local:9093","MyTopic","SDK-Client3");
/*
        System.out.println(c);
        ConsumerRecords recs = c.poll(Duration.ofSeconds((1)));
        System.out.println(String.format("RecordsCount: %s, ",recs.count()));
        Iterator <ConsumerRecord<String,String>>iter = recs.iterator();
        System.out.println(iter);
        while (iter.hasNext()){
            ConsumerRecord cr = iter.next();
            System.out.println(String.format("offset = %d, key = %s, value = %s\n", cr.offset(), cr.key(), cr.value()));
        }

        c.close();*/

    }
}
