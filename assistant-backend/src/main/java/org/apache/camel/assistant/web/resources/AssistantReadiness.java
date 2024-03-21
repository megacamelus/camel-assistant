package org.apache.camel.assistant.web.resources;

import java.time.Duration;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import io.qdrant.client.grpc.Collections;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Readiness;

@Readiness
@ApplicationScoped
public class AssistantReadiness implements HealthCheck {
    @ConfigProperty(name = "qdrant.collection.name")
    String collectionName;
    @ConfigProperty(name = "qdrant.host")
    String host;
    @ConfigProperty(name = "qdrant.grpc.port", defaultValue = "6334")
    int port;

    private QdrantClient client;

    @PostConstruct
    void init() {
        this.client = new QdrantClient(QdrantGrpcClient.newBuilder(host, port, false).build());
    }

    @PreDestroy
    void cleanup() {
        if (this.client != null) {
            this.client.close();
        }
    }

    @Override
    public HealthCheckResponse call() {
        HealthCheckResponseBuilder rb = HealthCheckResponse.named("Qdrant connection health check")
            .withData("qdrant.host", host)
            .withData("qdrant.port", port)
            .withData("qdrant.collection", collectionName);

        try {
            Collections.CollectionInfo reply =
                client.getCollectionInfoAsync(collectionName, Duration.ofSeconds(5)).get();

            rb.withData("collection.status", reply.getStatus().name());

            switch (reply.getStatus()) {
                case UNRECOGNIZED:
                case UnknownCollectionStatus:
                case Yellow:
                case Red:
                    rb.down();
                    break;
                case Green:
                    rb.up();
                    break;
            }
        } catch (Exception e) {
            rb.withData("failure.message", e.getMessage());
            rb.down();
        }

        return rb.build();
    }
}
