package org.apache.camel.assistant.ui.vaadin;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import org.apache.camel.assistant.ui.common.UiConfiguration;
import org.jboss.logging.Logger;

@Dependent
public class ChatService {
    private static final Logger LOG = Logger.getLogger(ChatService.class);

    @Inject
    UiConfiguration configuration;

    public String send(String message) throws URISyntaxException, IOException, InterruptedException {
        LOG.infof("Sending a message to %s", configuration.backend().baseUrl());
        HttpClient client = HttpClient.newHttpClient();
        
        HttpRequest request = HttpRequest.newBuilder()
        .uri(new URI(configuration.backend().baseUrl() + ServiceConstants.CHAT_API_PATH))
        .headers("Content-Type", "text/plain;charset=UTF-8")
        .timeout(Duration.of(configuration.backend().timeout(), ChronoUnit.SECONDS))
        .POST(HttpRequest.BodyPublishers.ofString(message))
        .build();

        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

        return response.body();
    }
}
