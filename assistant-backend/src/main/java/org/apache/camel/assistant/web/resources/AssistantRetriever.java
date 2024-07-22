package org.apache.camel.assistant.web.resources;

import java.util.function.Supplier;

import jakarta.inject.Singleton;

import dev.langchain4j.model.embedding.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore;

@Singleton
public class AssistantRetriever implements Supplier<RetrievalAugmentor> {
    private final RetrievalAugmentor augmentor;

    AssistantRetriever(QdrantEmbeddingStore store, AllMiniLmL6V2EmbeddingModel model, AssistantConfiguration configuration) {
        EmbeddingStoreContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
            .embeddingModel(model)
            .embeddingStore(store)
            .maxResults(configuration.retriever().maxResults())
            .minScore(configuration.retriever().minScore())
            .build();

        augmentor = DefaultRetrievalAugmentor
            .builder()
            .contentRetriever(contentRetriever)
            .build();
    }

    @Override
    public RetrievalAugmentor get() {
        return augmentor;
    }

}
