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
import picocli.CommandLine;


@CommandLine.Command
public class Main  implements Runnable {

    @CommandLine.Option(names = {"--url"}, defaultValue = "http://localhost:8000/v1")
    String baseUrl;


    @CommandLine.Option(names = {"--api-key"}, defaultValue = "no_api_key")
    String apiKey;

    @CommandLine.Option(names = {"--model-type"}, defaultValue = "openai")
    String modelType;

    @CommandLine.Option(names = {"--model-name"})
    String modelName;

    @CommandLine.Parameters(index = "0")
    String question;

    @Override
    public void run() {
        ChatLanguageModel model = buildModel();

        final String answer = model.generate(question);

        System.out.println(answer);
    }

    public ChatLanguageModel buildModel() {
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
