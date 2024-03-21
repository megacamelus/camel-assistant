package org.apache.camel.assistant.web.resources;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.inject.Singleton;

@RegisterAiService(retrievalAugmentor = AssistantRetriever.class)
@Singleton
public interface CamelAssistant {
    @SystemMessage("Your task is to help answer technical questions about Apache Camel")
    String assist(@UserMessage String question);

}
