package org.apache.camel.assistant.ui.common;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "camel.assistant")
public interface UiConfiguration {

    Backend backend();

    interface Backend {

        @WithDefault("http://localhost:8080/api/hello")
        String url();

        @WithDefault("100")
        int timeout();
    }


}
