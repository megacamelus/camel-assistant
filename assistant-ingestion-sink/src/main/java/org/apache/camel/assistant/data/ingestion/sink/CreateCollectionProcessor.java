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

package org.apache.camel.assistant.data.ingestion.sink;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.qdrant.client.grpc.Collections;
import io.quarkus.arc.Unremovable;
import org.apache.camel.CamelContext;
import org.apache.camel.CamelExecutionException;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.qdrant.Qdrant;
import org.apache.camel.component.qdrant.QdrantAction;
import org.apache.camel.component.qdrant.QdrantActionException;
import org.apache.camel.assistant.data.ingestion.common.IngestionSinkConfiguration;
import org.jboss.logging.Logger;

@ApplicationScoped
@Named("createCollectionProcessorBean")
@Unremovable
public class CreateCollectionProcessor implements Processor {
    private static final Logger LOG = Logger.getLogger(CreateCollectionProcessor.class);

    @Inject
    CamelContext context;

    @Inject
    IngestionSinkConfiguration configuration;

    @Override
    public void process(Exchange exchange) {
        LOG.infof("Checking if the collection exists");
        final ProducerTemplate producerTemplate = context.createProducerTemplate();

        try {
            producerTemplate.sendBodyAndHeader("qdrant:" + configuration.qdrant().collection().name(),
                    null, Qdrant.Headers.ACTION,
                    QdrantAction.COLLECTION_INFO);

        } catch (CamelExecutionException e) {
            createCollectionIfNotExists(e, producerTemplate);
        }
    }

    private void createCollectionIfNotExists(CamelExecutionException e, ProducerTemplate producerTemplate) {
        LOG.infof("Trying to create the collection in case it does not exist. %s", e.getCause().getClass());
        final Throwable cause = e.getCause();

        if (cause instanceof QdrantActionException actionException) {
            LOG.info("Camel Qdrant reported a failure. This is expected if the collection doesn't exist");

            if (actionException.getCause() instanceof StatusRuntimeException statusRuntimeException) {
                LOG.debug("Status Runtime exception detected");

                if (statusRuntimeException.getStatus().getCode() == Status.NOT_FOUND.getCode()) {
                    doCreateCollection(producerTemplate);
                }

            }
        }
    }

    private void doCreateCollection(ProducerTemplate producerTemplate) {
        LOG.infof("Trying to create the collection in case it does not exist.");

        try {
            producerTemplate.requestBodyAndHeader("qdrant:" + configuration.qdrant().collection().name(),
                    Collections.VectorParams.newBuilder().setDistance(Collections.Distance.Cosine).setSize(configuration.qdrant().size())
                            .build(),
                    Qdrant.Headers.ACTION,
                    QdrantAction.CREATE_COLLECTION);
        } catch (CamelExecutionException e) {
            LOG.error("Failed to create the new collection");
        }
    }
}
