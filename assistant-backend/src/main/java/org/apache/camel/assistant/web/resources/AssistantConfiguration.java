package org.apache.camel.assistant.web.resources;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "camel.assistant")
public interface AssistantConfiguration {

    Qdrant qdrant();

    interface Qdrant {
        Collection collection();

        @WithDefault("localhost")
        String host();

        @WithDefault("6334")
        int port();

        interface Collection {
            @WithDefault("camel")
            String name();
        }
    }
}
