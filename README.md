# camel-assistant

# Testing it using Docker Compose

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

4. Load some data into the Qdrant DB

```shell
cd assistant-cli && java -jar target/quarkus-app/quarkus-run.jar consume file /path/to/red_hat_build_of_apache_camel-4.0-tooling_guide-en-us.pdf
```
NOTE: you can download some PDFs from [here](https://github.com/megacamelus/cai/tree/main/docs).

5. Wait a few seconds and check if the data is available in the Qdrant DB

```shell
curl -X POST http://localhost:6333/collections/camel/points/scroll -H "Content-Type: application/json" -d "{\"limit\": 50 }" | jq .
```

6. Access the [web UI](http://localhost:8081/) and ask a question.