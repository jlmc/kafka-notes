
1. create index

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


2.We can see the status of all indexes, if everything is fine, everyone should have a green state:

`GET _cat/indices?v`

3. We can also see the health of the elastic cluster:
`GET _cat/health?v`
   
4. search 

GET twitter_tweets/_search

GET twitter_tweets/_doc/3OOZc3YBY0C1qIqeJaTD
