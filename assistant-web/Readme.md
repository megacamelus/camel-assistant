To run this application locally:

1. Start Qdrant container.
```shell
$ podman run -d -p 6334:6334 -p 6333:6333 qdrant/qdrant:v1.7.4-unprivileged
```

2. Create the Camel collections. (Note that the dimension corresponds to the all-MiniLM-L6-v2 Embedding Model used in the app)
```shell
curl -X PUT http://localhost:6333/collections/camel \
-H "Content-Type: application/json" \
-d '{
"vectors": {
"size": 384,
"distance": "Cosine"
}
}'
```

3. Start the app in dev mode. It will start Ollama devservices automatically. Loading the model would take several minutes.
```shell
mvn compile quarkus:dev
```

4. Start chatting with the assistant. 
```shell
curl -X POST http://localhost:8080/api/hello \
-H "Content-Type: text/plain" \
-d 'How do we create a timer with Apache Camel'
```
