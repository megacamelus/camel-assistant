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

import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentByCharacterSplitter;
import dev.langchain4j.data.document.splitter.DocumentByLineSplitter;
import dev.langchain4j.data.document.splitter.DocumentByParagraphSplitter;
import dev.langchain4j.data.document.splitter.DocumentBySentenceSplitter;
import dev.langchain4j.data.document.splitter.DocumentByWordSplitter;
import dev.langchain4j.model.Tokenizer;
import dev.langchain4j.model.openai.OpenAiTokenizer;
import org.apache.camel.Exchange;
import org.jboss.logging.Logger;

public final class SplitterUtil {
    private static final Logger LOG = Logger.getLogger(SplitterUtil.class);

    public static DocumentSplitter fromConfiguration(IngestionSourceConfiguration configuration, Tokenizer tokenizer) {
        final IngestionSourceConfiguration.Splitter splitterConf = configuration.splitter();
        return byName(splitterConf.name(), splitterConf.maxTokens(), splitterConf.maxOverlap(), tokenizer);
    }

    public static DocumentSplitter byName(String name, int maxTokens, int maxOverlap, Tokenizer tokenizer) {
        LOG.infof("Creating a %s splitter", name);
        switch (name) {
            case "sentence" -> {
                return new DocumentBySentenceSplitter(maxTokens, maxOverlap, tokenizer);
            }
            case "paragraph" -> {
                return new DocumentByParagraphSplitter(maxTokens, maxOverlap, tokenizer);
            }
            case "character" -> {
                return new DocumentByCharacterSplitter(maxTokens, maxOverlap, tokenizer);
            }
            case "word" -> {
                return new DocumentByWordSplitter(maxTokens, maxOverlap, tokenizer);
            }
            case "line" -> {
                return new DocumentByLineSplitter(maxTokens, maxOverlap, tokenizer);
            }
            default -> throw new IllegalArgumentException("Unknown splitter name: " + name);
        }
    }

    public static String[] split(DocumentSplitter splitter, String body) {
        if (splitter instanceof DocumentBySentenceSplitter ds) {
            return ds.split(body);
        } else {
            if (splitter instanceof DocumentByParagraphSplitter dp) {
                return dp.split(body);
            } else {
                if (splitter instanceof DocumentByCharacterSplitter dc) {
                    return dc.split(body);
                } else {
                    if (splitter instanceof DocumentByWordSplitter dw) {
                        return dw.split(body);
                    }
                }
            }
        }
        return null;
    }

    public static DocumentSplitter createDocumentSplitter(Exchange exchange, IngestionSourceConfiguration configuration) {
        final Tokenizer tokenizer = new OpenAiTokenizer();

        DocumentSplitter splitter;
        String splitterName = exchange.getMessage().getHeader(UserParams.SPLITTER_NAME, String.class);
        if (splitterName != null) {
            final IngestionSourceConfiguration.Splitter splitterConf = configuration.splitter();
            splitter = SplitterUtil.byName(splitterName, splitterConf.maxTokens(), splitterConf.maxOverlap(),
                    tokenizer);
        } else {
            splitter = SplitterUtil.fromConfiguration(configuration, tokenizer);
        }
        return splitter;
    }
}
