# kafka

[APACHE KAFKA QUICKSTART](https://kafka.apache.org/quickstart)


1. GET KAFKA, [Download](https://www.apache.org/dyn/closer.cgi?path=/kafka/2.6.0/kafka_2.13-2.6.0.tgz) the latest Kafka release and extract it:

```
$ tar -xzf kafka_2.13-2.6.0.tgz
$ cd kafka_2.13-2.6.0
```

2.  START THE KAFKA ENVIRONMENT,

- Run the following commands in order to start all services in the correct order:
  ```
  # Start the ZooKeeper service
  # Note: Soon, ZooKeeper will no longer be required by Apache Kafka.
  $ bin/zookeeper-server-start.sh config/zookeeper.properties
  ```
- Open another terminal session and run:
  ```
  $ bin/kafka-server-start.sh config/server.properties
  ```
- Once all services have successfully launched, you will have a basic Kafka environment running and ready to use.

3. CREATE A TOPIC TO STORE YOUR EVENTS

- Kafka is a distributed event streaming platform that lets you read, write, store, and process events (also called records or messages in the documentation) across many machines.

- Example events are payment transactions, geolocation updates from mobile phones, shipping orders, sensor measurements from IoT devices or medical equipment, and much more. These events are organized and stored in topics. Very simplified, a topic is similar to a folder in a filesystem, and the events are the files in that folder.

- So before you can write your first events, you must create a topic. Open another terminal session and run:

  ```
  $ bin/kafka-topics.sh --create --topic quickstart-events --bootstrap-server localhost:9092
  ```

-  All of Kafka's command line tools have additional options: run the kafka-topics.sh command without any arguments to display usage information. For example, it can also show you details such as the partition count of the new topic:

```
$ bin/kafka-topics.sh --describe --topic quickstart-events --bootstrap-server localhost:9092
Topic:quickstart-events  PartitionCount:1    ReplicationFactor:1 Configs:
Topic: quickstart-events Partition: 0    Leader: 0   Replicas: 0 Isr: 0
```


## Docker

[APACHE KAFKA QUICKSTART
WITH DOCKER](https: //kafka.apache.org/quickstart-docker#step-4-write-events)

1. CREATE A TOPIC TO STORE YOUR EVENTS
```
 $ docker exec -it kafka-broker kafka-topics.sh --create --topic quickstart-events
```

All of Kafka's command line tools have additional options: run the kafka-topics.sh command without any arguments to display usage information. For example, it can also show you details such as the partition count of the new topic:

```
  $ docker exec -it kafka-broker kafka-topics.sh --describe --topic quickstart-events
```


2. WRITE SOME EVENTS INTO THE TOPIC

A Kafka client communicates with the Kafka brokers via the network for writing (or reading) events. Once received, the brokers will store the events in a durable and fault-tolerant manner for as long as you need—even forever.

Run the console producer client to write a few events into your topic. By default, each line you enter will result in a separate event being written to the topic.

```
$ docker exec -it kafka-broker kafka-console-producer.sh --topic quickstart-events
This is my first event
This is my second event
```

3. READ THE EVENTS

Open another terminal session and run the console consumer client to read the events you just created:

```
$ docker exec -it kafka-broker kafka-console-consumer.sh --topic quickstart-events --from-beginning
This is my first event
This is my second event
```

You can stop the consumer client with Ctrl-C at any time.

Feel free to experiment: for example, switch back to your producer terminal (previous step) to write additional events, and see how the events immediately show up in your consumer terminal.

Because events are durably stored in Kafka, they can be read as many times and by as many consumers as you want. You can easily verify this by opening yet another terminal session and re-running the previous command again.



## Test the installation


- create the topic.
```
docker exec 0cf2c3c38f4f kafka-topics --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic EXAMPLE
```

```
kafka-topics --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic EXAMPLE
```

- create the producer
```
kafka-console-producer --broker-list localhost:9092 --topic EXAMPLE
```

- list the all topics
```
kafka-topics --bootstrap-server localhost:9092 --list
```


- create a consumer create a new consumer over the topic EXAMPLE start-consumer-message-since now
```
kafka-console-consumer --bootstrap-server localhost:9092 --topic EXAMPLE
```

- create a new consumer over the topic EXAMPLE start-consumer-message-since beginning
```
kafka-console-consumer --bootstrap-server localhost:9092 --topic EXAMPLE --from-beginning
```

Message-1
Message-2: hello each line is one message


---

# 1. Topics, Partitions and Offsets


- Topics:
    - A particular stream of data
    - base of everything.
    - it is basically similar to a table in a database.
        - it has a name, a topic is identiffied by its name.


- Partitions
    - topics are split into partitions
    - for one topic, we can have for example 3 partitions
    - these partitions have numbers and these numbers start at Zero
    - Each partition is ordered
    - Each message within a partition gets an incremental id, called offset

- Offset
    - have a meaning for a spefic partition
    - Order is garantted only within a partition (not across partitions)


- Data
    - The data in kafka is kept only for a limited time (default is one week)
    - But the offsets kepp on incrementing, they never go back to zero
    - Once the data is written to a partition it can not be changed.

Brokers
-  brocker = server
-





            | _ Partition 0
            |
Kafka Topic | _ Partition 1
            |
            | _ Partition 2


Comandos
---

> Create a producer

```shell script
kafka-console-producer.sh --broker-list localhost:9092 --topic LOJA_NOVO_PEDIDO
>Message-1
>Message-2: hello each line is one message
>Message-3: todos os consumidores sobre o mesmo topic vão consumir esta mensagem
```

> Producer with keys
```
kafka-console-producer --broker-list 127.0.0.1:9092 --topic first_topic --property parse.key=true --property key.separator=,
> key,value
> another key,another value
```


> create a consumer
```shell script
# create a new consumer over the topic LOJA_NOVO_PEDIDO start-consumer-message-since now
kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic LOJA_NOVO_PEDIDO

# create a new consumer over the topic LOJA_NOVO_PEDIDO start-consumer-message-since beginning
kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic LOJA_NOVO_PEDIDO --from-beginning
Message-1
Message-2: hello each line is one message
```

> Consumer with keys
```
kafka-console-consumer --bootstrap-server 127.0.0.1:9092 --topic first_topic --from-beginning --property print.key=true --property key.separator=,

```



List all consumer groups, discribe a consumer group, delete consumer group info or reset consumer group offsets

```
kafka-consumer-groups
```

```
kafka-consumer-groups bootstrap-server 127.0.0.1:9092 --list
```


```
kafka-consumer-groups bootstrap-server 127.0.0.1:9092 --describe --group <group-name>
```


```
kafka-consumer-groups bootstrap-server 127.0.0.1:9092 --describe --group <group-name>
```

Reset the ofset of one consumer group
```
kafka-consumer-groups bootstrap-server 127.0.0.1:9092 --group <group-name> --reset-offset --to-earliest --execute --topic <topic-name>
```

---

# Configuring Producers and Consumers

### Client Configurations

- There exist a lot of options to:
  - configure producer: https://kafka.apache.org/documentation/#producerconfigs
  - configure consumers:  https://kafka.apache.org/documentation/#consumerconfigs
