/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.camel.assistant.main;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;

public class Main {

    // Defaults to a locally installed, OpenAI-compatible API.
    public static final String baseUrl = System.getProperty("cai.url", "http://localhost:8000/v1/");

    // Defaults to a locally installed, OpenAI-compatible API, without the need of an API key
    public static final String apiKey = System.getProperty("cai.openai.key", "no_api_key");

    public static final String modelType = System.getProperty("cai.model.type", "openai");
    public static final String modelName = System.getProperty("cai.openapi.model.name");

    public static final String question = System.getProperty("cai.question");

    public static void main(String[] args) {
        ChatLanguageModel model = buildModel();

        final String answer = model.generate(question);
        System.out.println(answer);
    }

    public static ChatLanguageModel buildModel() {
        return switch (modelType) {
            case "openai" -> OpenAiChatModel.builder().baseUrl(baseUrl)
                    .apiKey(apiKey)
                    .modelName(modelName).build();
            case "ollama" -> OllamaChatModel.builder()
                    .baseUrl(baseUrl)
                    .modelName(modelName)
                    .build();
            default -> throw new UnsupportedOperationException("Unsupported model type: " + modelType);
        };
    }
}
