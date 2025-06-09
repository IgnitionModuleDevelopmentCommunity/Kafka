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

 - Ignition v.8.0.14 +

## Getting started
Compile and install the module.
Note: If you are installing an unsigned version of the module, ensure Ignition is configured in developer mode to allow unsigned modules to run. Alternatively you can sign the module and install the module into any gateway.

In the gateway, create a Kafka connection setting by navigating to *Kafka* -> *Kafka Connections*. Fill in the form with the paths to the key/trust stores, and their associated passwords. If the enable SSL checkbox is selected, these stores will be used to connect to the Kafka server with SSL.

### SSL Configuration
To configure SSL:
1. For client certs to be shared to the Kafka server, we'll re-use the cert in Ignition found in: `C:\Program Files\Inductive Automation\Ignition\webserver\ssl.key`
2. 
* If the Kafka server's cert is signed by a known CA, the truststore found in Java's trust store can be used. 
* Self signed certs will need the CA added to a new keystore, and the keystore needs to be placed in: `C:\Program Files\Inductive Automation\Ignition\data\truststore.jks`


## To Use
Simply to use:
```python
serverPath = 'server:9093'
kafkaName = 'myConnectionName' # the name of a Kafka setting in the gateway; if the connection doesn't exist, it will try the first available connection
consumer = system.kafka.getConsumer(kafkaName, serverPath,'topicname','groupname') 
for record in consumer:
	print record["value"], record["timestamp"],record["offset"],record["key"],record["partition"]
```
