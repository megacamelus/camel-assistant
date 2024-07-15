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

package org.apache.camel.assistant.data.ingestion.source.plaintext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import dev.langchain4j.data.document.DocumentSplitter;
import io.quarkus.arc.Unremovable;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.assistant.data.ingestion.source.common.IngestionSourceConfiguration;
import org.apache.camel.assistant.data.ingestion.source.common.SplitterUtil;
import org.apache.camel.assistant.data.ingestion.source.common.UserParams;
import org.apache.camel.builder.RouteBuilder;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.jboss.logging.Logger;

@ApplicationScoped
@Unremovable
public class PlainTextRoute extends RouteBuilder {
    private static final Logger LOG = Logger.getLogger(PlainTextRoute.class);

    private static final String DATA_SIZE = "DATA_SIZE";

    @Inject
    CamelContext context;

    @Inject
    IngestionSourceConfiguration configuration;

    private void convertBytesToPDFFile(Exchange e) throws IOException {
        final byte[] body = e.getMessage().getBody(byte[].class);

        // TODO: Camel should probably do this itself
        PDDocument document = Loader.loadPDF(body);

        String removePages = e.getMessage().getHeader(UserParams.REMOVE_PAGES, String.class);
        if (removePages != null) {
            LOG.infof("Removing pages %s", removePages);

            final List<Integer> pagesToRemove = getPagesToRemove(removePages);

            pagesToRemove.sort(Collections.reverseOrder());
            for (int page : pagesToRemove) {
                LOG.infof("Removing page %d", page);
                document.removePage(page);
            }
        }

        e.getMessage().setHeader(DATA_SIZE, body.length);
        e.getMessage().setBody(document);
    }

    private static List<Integer> getPagesToRemove(String removePages) {
        String[] ranges = removePages.split(",");
        List<Integer> pagesToRemove = new ArrayList<>();
        for (String range : ranges) {
            if (range.contains("-")) {
                String[] pages = range.split("-");
                int initialPage = Integer.parseInt(pages[0]);
                int endPage = Integer.parseInt(pages[1]);

                for (int i = initialPage; i < endPage; i++) {
                    pagesToRemove.add(i - 1);
                }
            } else {
                int page = Integer.parseInt(range);

                pagesToRemove.add(page - 1);
            }
        }
        return pagesToRemove;
    }

    private void chunkProcessor(Exchange exchange) {
        final String body = exchange.getMessage().getBody(String.class);
        final ProducerTemplate producerTemplate = context.createProducerTemplate();

        final DocumentSplitter splitter = SplitterUtil.createDocumentSplitter(exchange, configuration);
        final String[] parts = SplitterUtil.split(splitter, body);

        for (int i = 0; i < parts.length; i++) {
            producerTemplate.sendBody("kafka:ingestion", parts[i]);
        }
    }

    @Override
    public void configure() {
        rest("/api")
                .get("/hello").to("direct:hello")
                .post("/consume/text/static").to("direct:consumeTextStatic")
                .post("/consume/text/dynamic/{source}/{id}").consumes("application/octet-stream").to("direct:consumeTextDynamic")
                .post("/consume/pdf/static").to("direct:consumePdfStatic");

        from("direct:hello")
                .routeId("source-web-hello")
                .transform().constant("Hello World");


        from("direct:consumePdfStatic")
                .routeId("source-consume-pdf-static-route")
                .process(this::convertBytesToPDFFile)
                .pipeline()
                    .to("pdf:extractText")
                    .process(this::chunkProcessor);

        from("direct:consumeTextDynamic")
                .routeId("source-consume-text-dynamic-route")
                .log("Received ${body}")
                .setHeader("dynamic", constant("true"))
                .to("kafka:ingestion")
                .transform().constant("Dynamic data loaded");

        from("direct:consumeTextStatic")
                .routeId("source-consume-text-static-route")
                .log("Received ${body}")
                .to("kafka:ingestion")
                .transform().constant("Static data loaded");
    }
}
