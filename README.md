# Ignition Kafka Module 
(Apache Kafka)[https://kafka.apache.org/]

## Summary 
This module uses the Kafka 2.2.1 library, which is able to communicate with older versions of Kafka servers as well. The implementation here only includes the consumer part of the Kafka architecture. The producer could easily be extended following the existing implementation.

<abbr title="Remote Procedure Call">RPC</abbr> is used as the interface to maintain the Ignition gateway to be the only entity directly communicating with the Kafka server. Ignition clients will call RPC functions to perform the subscription.

I had also created a gateway global object to store the consumer objects in memory. This way the consumer object does not have to be created every time the poll is needed to execute. 

SSL has been included in the implementation.

## What is Kafka?
Kafka is a pub-sub protocol allowing for various counterparts to publish messages and subscribe to messages. 


## Requirements
You will need the following to use this module:

 - Ignition v.7.7.0 - Ignition v.7.9.x

## Getting started
Install the compiled module.

### SSL Configuration
To configure SSL:
1. For client certs to be shared to the Kafka server, we'll re-use the cert in Ignition found in: `C:\Program Files\Inductive Automation\Ignition_kafka\webserver\ssl.key`
2. 
* If the Kafka server's cert is signed by a known CA, the truststore found in Java's trust store can be used. 
* Self signed certs will need the CA added to a new keystore, and the keystore needs to be placed in: `C:\Program Files\Inductive Automation\Ignition\data\truststore.jks`


## To Use
Simply to use:
```python
serverPath = 'server:9093'
consumer = system.kafka.getSSLConsumer(serverPath,'topicname','groupname') # if SSL is desired
# consumer = system.kafka.getConsumer(serverPath,'topicname','groupname') # If ssl is not required
for record in consumer:
	print record["value"], record["timestamp"],record["offset"],record["key"],record["partition"]
```