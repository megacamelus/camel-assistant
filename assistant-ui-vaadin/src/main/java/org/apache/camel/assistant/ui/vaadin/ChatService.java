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

import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.Dependent;

@Dependent
public class ChatService {

    @ConfigProperty(name = "assistant-backend.url")
    String apiURL;

    @ConfigProperty(name = "assistant-backend.timeout.seconds")
    long timeout;

    public String send(String message) throws URISyntaxException, IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        
        HttpRequest request = HttpRequest.newBuilder()
        .uri(new URI(apiURL))
        .headers("Content-Type", "text/plain;charset=UTF-8")
        .timeout(Duration.of(timeout, ChronoUnit.SECONDS))
        .POST(HttpRequest.BodyPublishers.ofString(message))
        .build();

        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

        return response.body();
    }
}
