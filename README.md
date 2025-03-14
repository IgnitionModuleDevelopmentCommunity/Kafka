# Ignition Kafka Module 
(Apache Kafka)[https://kafka.apache.org/]

## Summary 
This module uses the Kafka ~~2.2.1~~ 3.7.1 library, which is able to communicate with older versions of Kafka servers as well. The implementation includes consumer and producer functions.

<abbr title="Remote Procedure Call">RPC</abbr> is used as the interface to maintain the Ignition gateway to be the only entity directly communicating with the Kafka server. Ignition clients will call RPC functions to perform the subscription.

Gateway global objects are used to store the consumer and producer objects in memory. This way the consumer/producer objects do not have to be created every time the consumer poll is needed to execute or whenn sending data to kafka.

SSL has been included in the implementation.  The function to add SSL to a properties object has been seperated so that it can be used by consumer and producer.

## What is Kafka?
Kafka is a pub-sub protocol allowing for various counterparts to publish messages and subscribe to messages. 


## Requirements
You will need the following to use this module:

 - Ignition v.8.0.14 +

## Getting started
Compile and install the module.
Note: If you are installing an unsigned version of the module, ensure Ignition is configured in developer mode to allow unsigned modules to run. Alternatively you can sign the module and install the module into any gateway.

### SSL Configuration
To configure SSL:
1. For client certs to be shared to the Kafka server, we'll re-use the cert in Ignition found in: `C:\Program Files\Inductive Automation\Ignition\webserver\ssl.key`
2. 
* If the Kafka server's cert is signed by a known CA, the truststore found in Java's trust store can be used. 
* Self signed certs will need the CA added to a new keystore, and the keystore needs to be placed in: `C:\Program Files\Inductive Automation\Ignition\data\truststore.jks`
** Edit : As of 8.1 Ignition changed the truststore path to: `C:\Program Files\Inductive Automation\Ignition\data\certificates`. 
   1. The extension on the "SSL file for the keystore was “.key” but the actual file extension needed to be “.pfx”
   2. Modify the password and update the source code to reflect the password for the truststore.  
** Credit/thanks to nicholas.robinson 


## To Use
Consumer Usage:
```python
serverPath = 'server:9093'
consumer = system.kafka.getSSLConsumer(serverPath,'topicname','groupname') # if SSL is desired
# consumer = system.kafka.getConsumer(serverPath,'topicname','groupname') # If ssl is not required
for record in consumer:
	print record["value"], record["timestamp"],record["offset"],record["key"],record["partition"]
```

Producer Usage:
```python
serverPath = "127.0.0.1:9092"
producerKey = system.kafka.createProducer(serverPath)
#producerKey = system.kafka.createSSLProducer(serverPath)

success = system.kafka.transmitData(
		producerKey,
		"firstTopic",
		"This data was produced by Ignition"
		)
				
if success:
	print("Data successfully sent")		
else:
	print("Issue encountered")	

```
