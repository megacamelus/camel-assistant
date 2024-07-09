# camel-assistant

## Trying it using Docker Compose

1. Start an OpenAI compatible API in a host accessible from the containers

```shell
OLLAMA_HOST=localhost:8000 ollama serve
```

2. Pull the `mistral:latest` model on the host you are running Ollama

```shell
ollama pull mistral:latest
```

3. Start the containers using Docker Compose

```shell
docker-compose up
```

4. Wait for everything to be up and then pull the `orca-mini` model: 

```shell
podman exec -it camel-assistant-ollama-1 ollama pull orca-mini
```

NOTE: this may take a while, as it needs to download about 2Gb of data from HuggingFace. 

5. While the model is downloading, use the time to load some data into the QDrant DB

```shell
cd assistant-cli && java -jar target/quarkus-app/quarkus-run.jar consume file /path/to/red_hat_build_of_apache_camel-4.0-tooling_guide-en-us.pdf
```
NOTE: you can download some PDFs from [here](https://github.com/megacamelus/cai/tree/main/docs).

6. Wait a few seconds and check if the data is available in the Qdrant DB

```shell
curl -X POST http://localhost:6333/collections/camel/points/scroll -H "Content-Type: application/json" -d "{\"limit\": 50 }" | jq .
```

7. Access the [web UI](http://localhost:8081/) and ask a question. Depending on your hardware, the reply may take up to 100 seconds.
Additionally, trying the assistant using docker compose on Mac is very slow, as the inference runs purely on CPU. 

## Trying it manually

### Requirements 

- A Kafka instance up and running and able to receive remote requests. 
- Podman installed and running (locally or remote)
- Ollama installed and running (locally or remote)

NOTE: URLs and hostnames can be configured in the `application.properties` file or exported via environment variables. For instance
if using Qdrant in another host, you can set its host using the `QDRANT_HOST` variable.

### Steps

1. Build the project

```shell
mvn clean package
```

2. Launch Qdrant:

```shell
podman run -d --rm --name qdrant -p 6334:6334 -p 6333:6333 qdrant/qdrant:v1.9.7-unprivileged
```

3. Launch Ollama:

```shell
OLLAMA_HOST=localhost:8000 ollama serve
```
NOTE: make sure you have the `mistral:latest` model available. If not, then download it using `OLLAMA_HOST=localhost:8000 ollama pull mistral:latest`.

4. Launch the ingestion sink: 

```shell
KAFKA_BROKERS=kafka-host:9092 java -jar ./assistant-ingestion-sink/target/quarkus-app/quarkus-run.jar
```

5. Launch the ingestion source:

```shell
KAFKA_BROKERS=kafka-host:9092 java -jar ./assistant-ingestion-sources/plain-text-source/target/quarkus-app/quarkus-run.jar
```

6. Launch the backend:

```shell
KAFKA_BROKERS=kafka-host:9092 java -jar ./assistant-backend/target/quarkus-app/quarkus-run.jar
```

7. Launch the UI:

```shell
java -jar assistant-ui-vaadin/target/quarkus-app/quarkus-run.jar
```