package org.apache.camel.assistant.web.resources;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "camel.assistant")
public interface AssistantConfiguration {

    Qdrant qdrant();

    Retriever retriever();

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

    interface Retriever {
        @WithDefault("3")
        int maxResults();

        @WithDefault("0.3")
        double minScore();
    }
}
