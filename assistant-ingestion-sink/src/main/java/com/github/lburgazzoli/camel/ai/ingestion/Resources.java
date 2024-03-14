package com.github.lburgazzoli.camel.ai.ingestion;

import dev.langchain4j.model.embedding.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
public class Resources {

    @Produces
    EmbeddingModel model() {
        return new AllMiniLmL6V2EmbeddingModel();
    }
}
