# Camel Assistant Web

# Setting up the environment

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


# Running locally with the default AI service using Quarkus dev mode

1. Start the app in dev mode. It will start Ollama devservices automatically. Loading the model would take several minutes.
```shell
mvn compile quarkus:dev
```

2. Start chatting with the assistant. 
```shell
curl -X POST http://localhost:8080/api/hello \
-H "Content-Type: text/plain" \
-d 'How do we create a timer with Apache Camel'
```


#` Running with a different AI service`

1. Edit the file `application.properties` and modify the following properties: 

```
quarkus.langchain4j.chat-model.provider=openai
quarkus.langchain4j.openai.api-key=no_api_key
quarkus.langchain4j.openai.base-url=http://localhost:8000/v1/
```

2. (Optional) Adjust other properties required for your environment

```
quarkus.langchain4j.openai.*.chat-model.model-name=some-other-model
```

3. Build the application 

```shell
mvn clean package
```

4. Then, to run the application:

```
mvn clean package 
java -jar target/quarkus-app/quarkus-run.jar
```