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
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.jboss.logging.Logger;

public class PlainTextRoute extends RouteBuilder {
    private static final Logger LOG = Logger.getLogger(PlainTextRoute.class);

    private static final String DATA_SIZE = "DATA_SIZE";

    // By default, Kafka won't accept payloads greater than 1048576,
    // give the additional data that may transit, we limit it to a much
    // lower value
    private static final int MAX_DATA_SIZE = 1000000;

    // AI APIs have an upper limit to the amount of tokens
    // they can accept. Typically this is 4096 ...
    // but given the context, question and everything else, we
    // use a much lower number
    private static final int MAX_TOKENS = 384;

    private void filter(Exchange exchange) {
        String unfilteredData = exchange.getMessage().getBody(String.class);

        exchange.getMessage().setBody(unfilteredData.replace('\n', ' '));
    }

    private void convertBytesToPDFFile(Exchange e) throws IOException {
        final byte[] body = e.getMessage().getBody(byte[].class);

        // TODO: Camel should probably do this itself
        PDDocument document = Loader.loadPDF(body);

        e.getMessage().setHeader(DATA_SIZE, body.length);
        e.getMessage().setBody(document);
    }

    public static class CustomSplitter {
        // TODO: this is very heavy and slow ... optimize this. Also, this is very
        //  poor at providing context.

        public List<String> largeDataSplitter(Exchange exchange) {
            final String body = exchange.getMessage().getBody(String.class);
            final String[] parts = body.split(" ");

            List<String> strings = new ArrayList<>();

            for (int i = 0; i < parts.length; i++) {
                StringBuffer sb = new StringBuffer();

                int r = 0;
                for (; r < MAX_TOKENS && r + i < parts.length && sb.length() < MAX_DATA_SIZE; r++) {
                    sb.append(parts[i + r]);
                    sb.append(" ");
                }
                i += r;

                strings.add(sb.toString());
            }

            LOG.infof("Finished splitting: %d parts", strings.size());
            return strings;
        }
    }



    @Override
    public void configure() throws Exception {
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
                    .process(this::filter)
                    .split(method(new CustomSplitter(), "largeDataSplitter")).streaming()
                    .to("kafka:ingestion?brokers={{bootstrap.servers}}");

        from("direct:consumeTextDynamic")
                .routeId("source-consume-text-dynamic-route")
                .setHeader("dynamic", constant("true"))
                .to("kafka:ingestion?brokers={{bootstrap.servers}}")
                .transform().constant("Dynamic data loaded");

        from("direct:consumeTextStatic")
                .routeId("source-consume-text-static-route")
                .to("kafka:ingestion?brokers={{bootstrap.servers}}")
                .transform().constant("Static data loaded");
    }
}
