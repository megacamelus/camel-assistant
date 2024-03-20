package org.apache.camel.assistant.web.resources;

import dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import org.eclipse.microprofile.config.inject.ConfigProperty;


@ApplicationScoped
public class AssistantEmbeddingStoreService {

    @ConfigProperty(name = "qdrant.collection.name")
    String collectionName;
    @ConfigProperty(name = "qdrant.host")
    String host;
    @ConfigProperty(name = "qdrant.grpc.port", defaultValue = "6334")
    int port;

    @Named
    QdrantEmbeddingStore store() {
            return QdrantEmbeddingStore.builder()
                    .collectionName(collectionName)
                    .host(host)
                    .port(port)
                    .useTls(false)
                    .build();
    }

}
