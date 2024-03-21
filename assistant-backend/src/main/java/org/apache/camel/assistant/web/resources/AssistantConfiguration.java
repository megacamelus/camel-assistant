package org.apache.camel.assistant.web.resources;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

@ConfigMapping
public interface AssistantConfiguration {

    Qdrant qdrant();

    interface Qdrant {
        @WithName("collection.name")
        String collectionName();

        @WithDefault("localhost")
        @WithName("host")
        String host();

        @WithDefault("6334")
        @WithName("grpc.port")
        int grpcPort();
    }
}
