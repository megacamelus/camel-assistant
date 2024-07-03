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

package org.apache.camel.assistant.data.ingestion.sink.routes;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.arc.Unremovable;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.qdrant.Qdrant;
import org.apache.camel.component.qdrant.QdrantAction;
import org.apache.camel.spi.DataType;

@ApplicationScoped
@Unremovable
public class KafkaToQdrantRoute extends RouteBuilder {

    @Override
    public void configure() {
        from("kafka:ingestion?brokers={{bootstrap.servers}}")
                .process("collectionProcessorBean")
                .to("langchain4j-embeddings:{{langchain.embedding.id}}")
                .setHeader(Qdrant.Headers.ACTION).constant(QdrantAction.UPSERT)
                .transform(new DataType("qdrant:embeddings"))
                .to("qdrant:{{qdrant.collection}}?host={{qdrant.host}}&port={{qdrant.port}}");
    }
}
