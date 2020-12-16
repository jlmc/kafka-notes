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


3. 
```
kafka-console-consumer --bootstrap-server localhost:9092 --topic FIRST_TOPIC --from-beginning
```
