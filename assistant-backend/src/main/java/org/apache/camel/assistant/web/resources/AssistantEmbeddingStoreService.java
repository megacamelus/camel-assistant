package org.apache.camel.assistant.web.resources;

import dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore;
import io.qdrant.client.QdrantClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;


@ApplicationScoped
public class AssistantEmbeddingStoreService {
    @Inject
    AssistantConfiguration configuration;
    @Inject
    QdrantClient client;

    @Named
    QdrantEmbeddingStore store() {
            return QdrantEmbeddingStore.builder()
                    .collectionName(configuration.qdrant().collectionName())
                    .client(client)
                    .build();
    }

}
