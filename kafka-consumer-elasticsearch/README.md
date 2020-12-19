# create and execute ES commands

1. Open Kibana DevTolls:
   - [Kibana dev tools](http://localhost:5601/app/dev_tools#/console)

2. create index

```
PUT twitter_tweets
```

Then:
```
PUT twitter_tweets/_settings
{
  "index": {
      "number_of_replicas": 0
  }
}
```


3.We can see the status of all indexes, if everything is fine, everyone should have a green state:

```
GET _cat/indices?v
```

4. We can also see the health of the elastic cluster:
```
GET _cat/health?v
```
   
5. Searching documents 
```
GET twitter_tweets/_search
GET twitter_tweets/_doc/3OOZc3YBY0C1qIqeJaTD
```


6. To increase total fields limit to 2000, try this
```
PUT twitter_tweets/_settings
{
  "index.mapping.total_fields.limit": 2000
}
```
