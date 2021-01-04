# Install Kafka

- [kafka](https://kafka.apache.org/downloads) 
- [Zookeeper](https://zookeeper.apache.org/)



# start kafka

 - _Important Note: Before we start Kafka we need to start the Zookeeper. Zookeeper will be used by kafka to store some important data_

 - Kafka already comes with the zookeeper version, if we don't have this software we can use the version that comes with kafka.

```shell script
cd ./kafka_2.13-2.4.1/
./bin/zookeeper-server-start.sh config/zookeeper.properties 

...
[2020-03-15 23:19:39,722] INFO binding to port 0.0.0.0/0.0.0.0:2181 (org.apache.zookeeper.server.NIOServerCnxnFactory)

```

We can see that the zookeeper is working in the port 2181 (witch is the default port)


```shell script
./bin/kafka-server-start.sh config/server.properties

...
    port = 9092
```

## Tests the installations

1. create a topic, note that the topic name should not contain '.' and the '_' at the same time
```shell script
cd ./kafka_2.13-2.4.1/

# create the topic
./bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic LOJA_NOVO_PEDIDO


# list all the topics
./bin/kafka-topics.sh --bootstrap-server localhost:9092 --list
```

2. create a producer 
```shell script
costax@costaxs-MBP kafka_2.13-2.4.1 % ./bin/kafka-console-producer.sh --broker-list localhost:9092 --topic LOJA_NOVO_PEDIDO
>Message-1
>Message-2: hello each line is one message
>Message-3: todos os consumidores sobre o mesmo topic vão consumir esta mensagem

```

3. create a consumer
```shell script
# create a new consumer over the topic LOJA_NOVO_PEDIDO start-consumer-message-since now
 ./bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic LOJA_NOVO_PEDIDO

# create a new consumer over the topic LOJA_NOVO_PEDIDO start-consumer-message-since beginning
 ./bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic LOJA_NOVO_PEDIDO --from-beginning
Message-1
Message-2: hello each line is one message

```


## Parelização de tarefas

- A mensagem será duplicada em todos os grupos os quais estão a escutar aquele topico
- Dentro de um grupo, quando chega uma nova mensagem, apenas um dos consumidores vai processar a mensagem.
- Problema: A questão é, Como é que o kafka paraleliza? Como podemos implementar dentro de um grupo existirem dois consumidores, no qual um deles recebe um tipo de mensagens e o outro recebe outro tipo de mensagem? 
  - Isso é feito atraves das partitions.
  - Para configurar as partições por defeito devemos ir ao ficheiro `server.properties` e editar a propriedade:
    ```properties
    # The default number of log partitions per topic. More partitions allow greater
    # parallelism for consumption, but this will also result in more files across
    # the brokers.
    num.partitions=1
    ```
  - O valor o defeito é 1, isso significa que, por padrão/defeito por cada topico existe uma unica pilha de mensagens (no kafka chama-se patição), posto isso todas as mensagens vão para essa partição.
  - Há que saber tambem que: 
    - Um consumidor é responsavel por uma ou mais partições.
    - Uma partição pode ter apenas um consumidor de um mesmo grupo.
  - Conclusão: 
    - Não adianta ter mais de um consumir por grupo de consumidor, se existir apenas uma partição
    - O numero de consumidores por grupo de consumo, não deve ser, superior ao numero de partições.
  - Podemos alterar incrementar o valor da propriedade, por exemplo para 3. Mas isso não afacetará os topicos já existentes, apenas os novos que serão criados entretatanto.
  - Podemos tambem reparticionar um topico já existente:
    ```shell script
    
    bin/kafka-topics.sh --bootstrap-server localhost:9092 --describe
    
    bin/kafka-topics.sh --alter \
                        --zookeeper localhost:2181 \
                        --topic ECOMMERCE_NEW_ORDER \
                        --partitions 5
    
    bin/kafka-topics.sh --bootstrap-server localhost:9092 --describe
    ```
  - Após isso podemos adicionar mais consumidores de um determinado grupo para um topico, desde que não adicionemos mais consumidores do que partições.
  - O kafka escolherá o melhor o consumidor com base no record key, isso signifca que, se enviarmos sempre a mesma chave em todas as mensagens, essas mensagens serão sempre entregue ao mesmo consumidor. 
  - Podemos tambem analisar o grupos de consumidores:
    ```shell script
    bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --all-groups
    ```
    
    
# 4. Serialização

## 4.1. Pastas do kafka e zookeeper

Por defeito o zookeeper e o kafka persistem os dados em pastas temporárias do Sistema operativo. 

- Para experimentar e fazer exemplos basicos esta optimos e é mais do que suficiente. 
- No entanto caso se reinicie a maquina os dados (entre os quais as mensagens) são perdidos e temos de recomeçar de novo.
- Se quisermos garantir a persistencia das mensagens devemos configurar uma pasta.


1. Criar as Pastas
```shell script
$ pwd
  /Users/costax/Documents/junk/app-servers/kafka/kafka_2.13-2.4.1

$ mkdir data
$ mkdir data/zookeeper
$ mkdir data/kafka

```

2. Configurar o kafka server properties (kafka_2.13-2.4.1/config/server.properties)
```properties
# A comma separated list of directories under which to store log files
# log.dirs=/tmp/kafka-logs
log.dirs=/Users/costax/Documents/junk/app-servers/kafka/kafka_2.13-2.4.1/data/kafka
```

3. Configurar zookeeper properties (kafka_2.13-2.4.1/config/zookeeper.properties)

```properties
# dataDir=/tmp/zookeeper
dataDir=/Users/costax/Documents/junk/app-servers/kafka/kafka_2.13-2.4.1/data/zookeeper
```

4. Podemos opcionalmente remover as pastas default

```shell script
$ rm -rf /tmp/kafka-logs
$ rm -rf /tmp/zookeeper 

```

5. Ao fazer estas configurações devemos ter em atenção que tudo será novo, portanto não termos qualquer topico.