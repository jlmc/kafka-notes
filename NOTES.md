1. create a topic in kafka server

```
kafka-topics --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 3 --topic FIRST_TOPIC
```

2. create a consumer in kafka server
```
kafka-console-consumer --bootstrap-server localhost:9092 --topic FIRST_TOPIC --from-beginning
```

or 
```
docker exec -it kafka1 kafka-console-consumer --bootstrap-server 127.0.0.1:9092  --topic FIRST_TOPIC --from-beginning```


kafka-console-consumer --bootstrap-server localhost:9092 --topic FIRST_TOPIC --from-beginning
```


# Configurations


## producers

- `acks`
  - acks means acknowledgments.  
  - that there were three level for acks.
    - There was zero, one, and all.
      
  - `0` (no acks)
    - no response from the broker, means that if the broker is or goes offline or an exceptions happens we won't know and we will lose data.
    - good feet for example for:
      - Metrics Collection
      - Log collection
      - or any other not critical data
    
  - `1` (default)
    - leader acknowledgment, basically response is requested, but there is no guaranty of any replication.
    - Replication is something that happens in the background but is not prerequisite to receive a response.
    - If an ACK is not received the producer may retry.
    - If the leader broker goes offline but replicas haven't replicated the data yet, we have a data loss.
    
 - `all` (replicas acknowledgment)
    - The leader and the replicas ack requested
    - Add some latency and safety
    - **When we use `aks` equals to `all`, we need also to use another setting called `min.insync.replicas` .** 
    - `min.insync.replicas` can be set at broker or topic level (override)
    - The most common setting for `min.insync.replicas` is 2. That are ISR -  (including leader) must respond that they have the data.
      - That means if you use `replication-factor=3,min.insync.replicas=2,acks=all`, you can only tolerate 1 broker down, otherwise the producer will receive an exception on send.
    
- `Retries`
  -  In case of transient failure, developer are expected to handle exceptions, otherwise the data will be lost.
    - Example of transient failure
      - NotEnoughReplicasException
    - There is a `retries` setting
      - default to 0 for kafka <= 2.0 (no retries)
      - default to 2147483647 for kafka >= 2.1
    - the is also the setting `retry.backoff.ms` that by default is 100ms. means that every 100 milliseconds kafka producer will retry to send to kafka.
    - to not perform retries forever, exists also other setting `deivery.timeout.ms` by default  have the value 120_000ms == 2 minutes, means the producer will perform retries at maximumm in 2 minutes.
  - _Warning: when we use reties there is a chance that messages will be sent out of order. If you retry-based ordering, that can be an issue. For this, you an set the setting while controls how many produce requests can be made in parallel: `max.in.flight.request.per.connection` (default = 5) in this case we should set the value to 1 _
    

### Idempotent Producer

Problem: The producer can introduce duplicate messages in kafka due to network errors (for example).

```
producerProps.put("enable.idempotence", true);
```

## In Summary, A good Safe producer:

-  kafka < 0.11
  - acks=all (ensure data is properly replicated before an ack is received)
  - min.insync.replicas=2
  - reties=MAX_INT (producer level)
  - max.in.flight.request.per.connection=1 (options, only if the ordering is an issue, otherwise the value should be 5)

-  kafka >= 0.11
  - enable.idempotence=true (producer level) 
  - min.insync.replicas=2 (broker/topic level)