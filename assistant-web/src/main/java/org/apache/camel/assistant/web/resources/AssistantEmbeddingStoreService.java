package org.apache.camel.assistant.web.resources;

import static dev.langchain4j.data.document.splitter.DocumentSplitters.recursive;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.eclipse.microprofile.config.inject.ConfigProperty;


@ApplicationScoped
public class AssistantEmbeddingStoreService {

    @ConfigProperty(name = "qdrant.collection.name")
    String collectionName;
    @ConfigProperty(name = "qdrant.host")
    String host;
    @ConfigProperty(name = "qdrant.grpc.port", defaultValue = "6334")
    String port;

    @Named
    QdrantEmbeddingStore store() {
            return QdrantEmbeddingStore.builder()
                    .collectionName(collectionName)
                    .host(host)
                    .port(Integer.valueOf(port))
                    .useTls(false)
                    .build();
    }

}
