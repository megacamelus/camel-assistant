package org.apache.camel.assistant.web.resources;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
public class AssistantProducers {
    @Produces
    public QdrantClient qdrantClient(AssistantConfiguration configuration) {
        QdrantGrpcClient gc = QdrantGrpcClient.newBuilder(
                configuration.qdrant().host(),
                configuration.qdrant().port(),
                false)
            .build();

        return new QdrantClient(gc);
    }
}
