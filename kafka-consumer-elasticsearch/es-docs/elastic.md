# How to run this project

Table of Contents
- Infrastructure
- setup
- Build and Run the application
- Appendix
    - Elasticsearch Queries


## Infrastructure

All the infrastructure used to run this project is defined/configured in the file [docker-compose.yml](../../docker-compose.yml)

Currently what is needed to run the project is:
- `Postgres:11.4`
    - we are using version 11.4, but there are no known retractions regarding the use of higher versions.

- `Elasticsearch:6.8.1`
    - Version 6.8.1 is up to current data (2020-04-16), the fully supported version of the most recent version of [Spring-Boot-2.2.6.RELEASE](https://github.com/spring-projects/spring-boot/releases/tag/v2.2.6.RELEASE).
    - It is not recommended upgrade the elasticsearch version without the spring-boot being able to fully support it.
    - We can see all versions that are supported by spring-boot, we just need to open the file:
        - spring-boot-dependencies-X.X.X.RELEASE.pom
        - The file can be found in the maven repositories:
            - [Central - spring-boot-dependencies-2.2.6.RELEASE.pom](https://repo1.maven.org/maven2/org/springframework/boot/spring-boot-dependencies/2.2.6.RELEASE/spring-boot-dependencies-2.2.6.RELEASE.pom)
            - [Local - spring-boot-dependencies-2.2.6.RELEASE.pom](/.m2/repository/org/springframework/boot/spring-boot-dependencies/2.2.6.RELEASE/spring-boot-dependencies-2.2.6.RELEASE.pom)
    - We can check if we have some instance of the elasticsearch running in your local machine in the web browser: http://localhost:9200/

- `kibana:6.8.1`
    - The kibana version should always be the same version of the elasticsearch for compatible reasons.
    - The Kibana is used in the project as a possible management tool for indexes elasticsearch, as well as to facilitate the implementation of search queries.
    - we can open it in the web browser: http://localhost:5601/


## Setup

1. In the project root folder, run the `docker-compose up` command to start all the required infrastructure instances. The docker compose will create an hidden folder `.volume` to store all the data in the instances, this way we can have an non-volatile environment.
    ```
    docker-compose up
    ```

2. Create elasticsearch index, we can use the kibana devtool or simply execute a curl command:
    1. Using Curl command (In the project root folder)
        - First:

            ```
            curl -XPUT -vi "http://localhost:9200/demo2" -H 'Content-Type: application/json' --data "@my_index2_mapping-shemma.json.json
            ```
        - Then:
            ```
            curl -XPUT "http://localhost:9200/demo2/_settings" -H 'Content-Type: application/json' -d'
            {
                  "index": {
                    "number_of_replicas": 0
                  }
            }'
            ```
        - We can see the status of all indexes, if everything is fine, everyone should have a green state:
            ```
            curl -XGET "http://localhost:9200/_cat/indices?v"
            ```
        - We can also see the health of the elastic cluster:
            ```
            curl -XGET "http://localhost:9200/_cat/health?v"
            ```
        - Helper command to fix yellow index:
            ```
            curl -XPUT "http://localhost:9200/demo2/_settings" -H 'Content-Type: application/json' -d'
            {
                "index": {
                    "number_of_replicas": 0
                }
            }'
            ```
        - Get Document by id
            ```
            curl -XGET "http://localhost:9200/demo2/_doc/XBNeg3EBnHG-g8dwRfTt"
            ```
        - Enable Elasticsearch Log queries:
            ```
             curl -XPUT 'http://localhost:9200/demo2/_settings?preserve_existing=true' -H 'Content-Type: application/json' -d '{
               "index.search.slowlog.level" : "trace",
               "index.search.slowlog.threshold.query.trace" : "5ms"
             }'
            ```

    2. Using Kibana > dev Tools
        - Basically are the same commands of previous point but executed in the kibana dev tools


## Build and Run the application

1. build the solution source code:
    ```
    mvn clean package
    ```

2. We can run the application using `demo` profile, this way all the demo data will be populated in the database and in the elasticsearch every time we start the application.
    ```
    export SPRING_PROFILES_ACTIVE=demo
    ```   
   or, we can use program arg parameter on startup
    ```
    java -jar -Dspring.profiles.active=demo target/elasticsearch-demo-0.0.2-SNAPSHOT.jar
    ```

3. start up the application
    ```
    java -jar target/elasticsearch-demo-0.0.2-SNAPSHOT.jar -Dagentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8000
    ```
   or
    ```
    mvn spring-boot:run -Dagentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8000
    ```

    - The application will create all postgres schema and fill it with all default data automatically using flyway spring boot plugin.

    - **TODO**: _at this moment the index in the elasticsearch still empty, therefore, it not contains any document, we can also consider populate the index at the application startup._

## Flow Examples:

1. Populate the elasticsearch index
```
curl --location --request POST 'http://localhost:8080/availabilities/mapping' \
--header 'Content-Type: application/json' \
--data-raw '{
    "from": "2020-01-01",
    "to": "2020-04-04"
}'
```


2. Search Suggestion for the criteria
```
curl --location --request GET 'http://localhost:8080/suggestions?examCode=328&dayMin=2020-02-03&dayMax=2020-02-03&hourMin=09:30&hourMax=12:30&city=Coimbra&dependencyCode=230&daysOfWeek=MONDAY,TUESDAY' \
--header 'X-USER: user2'
```

3. Create appointment

```
curl --location --request POST 'http://localhost:8080/appointments' \
--header 'X-USER: user1' \
--header 'Content-Type: application/json' \
--data-raw '{
    "day": "2020-02-03",
    "start": "12:30",
    "examCode": "328",
    "roomId": 1,
    "patient": "Duke master"
}
'
```


# Appendix

## Elasticsearch Queries
- Health
    ```
    # see all indexs
    GET _cat/indices?v
        
    # see cluster health
    GET _cat/health?v
    ```

- The 100 firsts documents in the day `2020-01-01` for the `roomId = 1`
    ```
    GET demo2/_search
    {
          "from": 0,
          "size": 100,
          "query": {
            "bool": {
              "must": [
                {
                  "match": {
                    "roomId": 1
                  }
                },
                {
                  "match": {
                    "day": "2020-01-01"
                  }
                }
              ]
            }
          }
    }
    ```
- Get document by Id = `oRPAhXEBnHG-g8dwt_i8`
    ```
    GET demo2/_doc/oRPAhXEBnHG-g8dwt_i8
    ```
- delete Index `demo2`, similar to the sql command `drop database demo2`
    ```
    DELETE demo2
    {}
    ```
- delete documents from index, similar to the sql command `delete from demo2 where day <= '1900-12-31' `
    ```
    POST demo2/_delete_by_query
    {
          "query": {
            "range": {
              "day": {
                "lte": "1900-12-31"
              }
            }
          }
    }
    ```

- Delete document by Id, similar to `delete from demo1 where id = 1234`
    ```
    DELETE /demo2/_doc/123
    {}
    ```

- Get o schema mapping de um index
    ```
    GET demo2/_mapping/_doc
    ```

- Add Document to the index ()
    ```
    POST /demo2/_doc/
    {
      "roomId": 1234
      ... other properties
    }
    ```
  or, using the PUT command, the id in the PUT command is mandatory. If a document with this id already exists, the document will be replaced. If a document with this id does not exist, it will be created.
    ```
    PUT /demo2/_doc/1234
    {
      "roomId": 1234
          .... other properties
    }
    ```

- Partial update of a document, to update, the roomId attribute of the document with Id=id 1 to 2, The command and URI we should use is:
    ```
    POST /demo/_doc/1/_update
    {
       "doc": {
           "roomId": 2
       }
    }
    ```


#### Relational database vs ElasticSearch

| DataBase             | ElasticSearch |
|----------------------|---------------|
| Instance             | Index         |
| Table                | Type          |
| Schema(desc table)   | Mapping       |
| Tuple                | Document      |
| Column               | Attribute     |


#### Search headers

- took : duração da consulta em milissegundos.
- time_out : valor booleano indicando se a consulta foi abortada por limite de tempo ou não.
- _shards.total : número de shards envolvidas na busca.
- _shards.successful : número de shards que não apresentaram falha durante a busca.
- _shards.failed : número de shards que apresentaram falha durante a busca. Note que o resultado pode não
  apresentar documentos presentes nas shards que falharam.
- hits.total : quantidade de documentos encontrados.
- hits.max_score : valor máximo de score entre os documentos encontrados. O valor 1.0 significa que o valor da
  busca foi encontrado totalmente.
- hits.hits : os documentos encontrados.


#### Query DSL

- **NOTA IMPORTANTE**: Query sobre campos analizados, Filtros sobre valores originais.
- Ao executar uma query recebemos em cada resultado um atributo `_score` que indica a similaridade ou relevância do documento com nossa busca.
- Por exemplo, executando no DevTools  do Kibana a query: `GET /demo2/_doc/_search?q=coimbra`
    - Recebemos os resultados com o atributos `_score` que define a similaridade:

- Ou seja:
    - Query:
        - procura-se o score tendo em conta os criterios de procura. Por exemplo:
            ```
            GET produtos/v1/_search?q=digital
            ```
        - O resultado, em cada item de hits encontramos o campo `_score` o que nos diz, o qual é o valor de aproximação aos criterios de pesquisa.
        - Os resultados são ordenados por score. o mais alto será o primeiro.
        - Não têm cache.

    - Filtos:
        - São procuras binarias, sim ou não.
        - O resultado não contem `_score`.
        - é possivel fazer o order by.
        - Cacheam os resultados (queries não).
        - Não possuem _score, pois só interesse se existe ou não (resultado binário)
        - Caso desejamos uma ordem, devemos definir na busca.
        - Filter , como must , must_not e should , sempre deve ser embrulhado pelo elemento bool. O interessante é que podemos combinar o filter com as outras clausulas. Isso é algo muito comum no dia a dia. Filtramos os documentos para aplicar depois uma query . Nesse exemplo, vamos zltrar os produtos pelo preço para depois pesquisar pela categoria

## Examples

- select everthing `from produtos`

    ```
    GET /produtos/v1/_search
      {
        "query": {
          "match_all": {}
        }
      }
    ``` 
- Equivalente a: `tags = impresso AND nome:scala`
    ```
    GET /produtos/v1/_search
    {
        "query" : {
            "bool": {
                "must": [
                    {"match": {"tags": "impresso" }},
                    {"match": {"nome": "scala" }}
                ]
            }
        }
    }        
    ```
- Equivalente a: `categoria = livro AND (tags = imutabilidade OR tags = larga escala) AND NOT(nome = scala)`
    ```
    GET /produtos/v1/_search
      {
        "query": {
          "bool": {
            "must": {
              "match": {
                "categoria": "livro"
              }
            },
            "should": [
              {
                "match": {
                  "tags": "imutabilidade"
                }
              },
              {
                "match": {
                  "tags": "larga escala"
                }
              }
            ],
            "must_not": {
              "match": {
                "nome": "scala"
              }
            }
          }
        }
      }
    ```



### Number of shards in one index

The number of shards of an index is defined at the time of its creation, and cannot be changed.
That is why it is very important to choose the number of shards well when creating the index.
The number of shards depends on the volume of information that will be stored in the shards, so that volume is divided by the amount of shards desired, thus seeing how big each shard will be.

In practice, this means that having many shards will assist at the time of writing, but the reading performance can be affected, because the search will have to read many shards to confirm whether the information was found or not.
That is why it depends on the frequency in which it is written and read in the shard, but a shard must not exceed the volume of 50 gb.