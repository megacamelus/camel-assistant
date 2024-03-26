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

import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.transaction.Transactional;

import io.qdrant.client.PointIdFactory;
import io.qdrant.client.grpc.Points;
import io.quarkus.arc.Unremovable;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.assistant.data.ingestion.sink.entities.Mapping;
import org.apache.camel.assistant.data.ingestion.sink.repository.MappingRepository;
import org.apache.camel.component.kafka.KafkaConstants;
import org.apache.camel.component.qdrant.Qdrant;
import org.apache.camel.util.ObjectHelper;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.jboss.logging.Logger;

@ApplicationScoped
@Named("collectionProcessorBean")
@Unremovable
@Transactional
public class CollectionProcessor implements Processor {
    private static final Logger LOG = Logger.getLogger(CollectionProcessor.class);
    private static final String EXTERNAL_ID = "id";
    private static final String EXTERNAL_SOURCE = "source";
    private static final String DYNAMIC = "dynamic";

    @Inject
    MappingRepository mappingRepository;

    @Override
    public void process(Exchange exchange) throws Exception {
        Message message = exchange.getMessage();

        logHeaders(message);

        boolean dynamic = message.getHeader(DYNAMIC, false, Boolean.class);
        if (dynamic) {
            String externalId = message.getHeader(EXTERNAL_ID, String.class);
            String externalSource = message.getHeader(EXTERNAL_SOURCE, String.class);

            if (ObjectHelper.isEmpty(externalId) || ObjectHelper.isEmpty(externalSource)) {
                // fail
            }

            // Check if an internal ID exists on the DB
            Points.PointId id;
            Mapping mapping = mappingRepository.findByExternal(externalId, externalSource);
            if (mapping == null) {
                // ... if it does not, then create a new internal ID and persist it on the DB
                final UUID uuid = createNewRecord(message);

                id = PointIdFactory.id(uuid);
            } else {
                LOG.infof("Found a previous record of this on the DB with internal_id = %s", mapping.internalId);

                // otherwise, use that ID to replace the value on the vector DB
                id = PointIdFactory.id(UUID.fromString(mapping.internalId));
            }

            message.setHeader(Qdrant.Headers.POINT_ID, id);
        }
    }

    private UUID createNewRecord(Message message) {
        final UUID uuid = UUID.randomUUID();
        Mapping mapping;
        mapping = new Mapping();

        mapping.internalId = uuid.toString();
        mapping.externalId = message.getHeader("id", String.class);
        mapping.externalSource = message.getHeader("source", String.class);

        LOG.infof("There is no previous record of this on the DB, therefore created a new one with internal_id = %s",
                mapping.internalId);

        mappingRepository.persist(mapping);

        return uuid;
    }

    private static void logHeaders(Message message) {
        var headers = message.getHeaders();

        for (var entry : headers.entrySet()) {
            LOG.infof("Consuming header: %s = %s", entry.getKey(), entry.getValue());
        }

        Headers kafkaHeaders = message.getHeader(KafkaConstants.HEADERS, Headers.class);
        for (Header header : kafkaHeaders) {
            LOG.infof("Consuming Kafka header: %s = %s", header.key(), new String(header.value()));
        }
    }
}
