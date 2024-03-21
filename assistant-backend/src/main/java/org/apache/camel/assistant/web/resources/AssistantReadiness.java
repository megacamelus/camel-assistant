package org.apache.camel.assistant.web.resources;

import java.time.Duration;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Collections;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Readiness;

@Readiness
@ApplicationScoped
public class AssistantReadiness implements HealthCheck {
    @Inject
    AssistantConfiguration configuration;
    @Inject
    QdrantClient client;

    @Override
    public HealthCheckResponse call() {
        HealthCheckResponseBuilder rb = HealthCheckResponse.named("Qdrant connection health check")
            .withData("qdrant.host", configuration.qdrant().host())
            .withData("qdrant.port", configuration.qdrant().port())
            .withData("qdrant.collection", configuration.qdrant().collection().name());

        try {
            Collections.CollectionInfo reply =
                client.getCollectionInfoAsync(configuration.qdrant().collection().name(), Duration.ofSeconds(5)).get();

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
