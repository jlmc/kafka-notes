# Read tweets from Twitter and send hims to a kafka topic

1. create a topic:
```
kafka-topics --zookeeper zoo1:2181 --create --topic twitter_tweets --partitions 4 --replication-factor 1
```

2. Create a dummy consumer
```
kafka-console-consumer --bootstrap-server 127.0.0.1:9092 --topic twitter_tweets
```

3. Create a dummy producer
```
kafka-console-producer --broker-list localhost:9092 --topic twitter_tweets
```