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

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;

public class PlainTextRoute extends RouteBuilder {

    private void convertBytesToPDFFile(Exchange e) throws IOException {
        // TODO: Camel should probably do this itself
        PDDocument document = Loader.loadPDF(e.getMessage().getBody(byte[].class));

        e.getMessage().setBody(document);
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
                    .to("direct:consumeTextStatic");

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
