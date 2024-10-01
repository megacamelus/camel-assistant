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

package org.apache.camel.assistant.data.ingestion.source.common;

import org.apache.camel.builder.TokenizerBuilderFactory;
import org.apache.camel.model.tokenizer.LangChain4jTokenizerDefinition;

public final class SplitterUtil {

    public static LangChain4jTokenizerDefinition createTokenizer(TokenizerBuilderFactory tokenizerBuilderFactory, IngestionSourceConfiguration configuration) {
        final IngestionSourceConfiguration.Splitter splitterConf = configuration.splitter();
        final String name = splitterConf.name();

        switch (name) {
            case "sentence" -> {
                return tokenizerBuilderFactory.bySentence()
                        .maxTokens(splitterConf.maxTokens())
                        .maxOverlap(splitterConf.maxOverlap())
                        .using(LangChain4jTokenizerDefinition.TokenizerType.valueOf(splitterConf.name()))
                        .end();
            }
            case "paragraph" -> {
                return tokenizerBuilderFactory.byParagraph()
                        .maxTokens(splitterConf.maxTokens())
                        .maxOverlap(splitterConf.maxOverlap())
                        .using(LangChain4jTokenizerDefinition.TokenizerType.valueOf(configuration.tokenizer().name()))
                        .end();
            }
            case "character" -> {
                return tokenizerBuilderFactory.byCharacter()
                        .maxTokens(splitterConf.maxTokens())
                        .maxOverlap(splitterConf.maxOverlap())
                        .using(LangChain4jTokenizerDefinition.TokenizerType.valueOf(configuration.tokenizer().name()))
                        .end();
            }
            case "word" -> {
                return tokenizerBuilderFactory.byWord()
                        .maxTokens(splitterConf.maxTokens())
                        .maxOverlap(splitterConf.maxOverlap())
                        .using(LangChain4jTokenizerDefinition.TokenizerType.valueOf(configuration.tokenizer().name()))
                        .end();
            }
            case "line" -> {
                return tokenizerBuilderFactory.byLine()
                        .maxTokens(splitterConf.maxTokens())
                        .maxOverlap(splitterConf.maxOverlap())
                        .using(LangChain4jTokenizerDefinition.TokenizerType.valueOf(configuration.tokenizer().name()))
                        .end();
            }
            default -> throw new IllegalArgumentException("Unknown splitter name: " + name);
        }
    }
}
