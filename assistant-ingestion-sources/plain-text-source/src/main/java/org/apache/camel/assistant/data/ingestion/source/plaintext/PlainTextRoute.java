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

import org.apache.camel.builder.RouteBuilder;

public class PlainTextRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        rest("/api")
                .get("/hello").to("direct:hello")
                .post("/consume/static").to("direct:consumeStatic")
                .post("/consume/dynamic/{source}/{id}").to("direct:consumeDynamic");

        from("direct:hello")
                .routeId("source-web-hello")
                .transform().constant("Hello World");

        from("direct:consumeDynamic")
                .routeId("source-consume-dynamic-route")
                .setHeader("dynamic", constant("true"))
                .to("kafka:ingestion?brokers={{bootstrap.servers}}")
                .transform().constant("Dynamic data loaded");

        from("direct:consumeStatic")
                .routeId("source-consume-static-route")
                .to("kafka:ingestion?brokers={{bootstrap.servers}}")
                .transform().constant("Static data loaded");
    }
}
