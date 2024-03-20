package org.apache.camel.assistant.web.resources;

import java.util.List;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.retriever.EmbeddingStoreRetriever;
import dev.langchain4j.retriever.Retriever;
import dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AssitantRetriever implements Retriever<TextSegment> {
    private final EmbeddingStoreRetriever retriever;

    AssitantRetriever(QdrantEmbeddingStore store, AllMiniLmL6V2EmbeddingModel model) {
        retriever = EmbeddingStoreRetriever.from(store, model, 10);
    }

    @Override
    public List<TextSegment> findRelevant(String s) {
        return retriever.findRelevant(s);
    }
}
