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

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.LongAdder;

import io.qdrant.client.PointIdFactory;
import io.qdrant.client.grpc.Points;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.component.qdrant.Qdrant;
import org.jboss.logging.Logger;

public class CollectionProcessor implements Processor {
    private static final LongAdder ADDER = new LongAdder();
    private static final Logger LOG = Logger.getLogger(CollectionProcessor.class);

    @Override
    public void process(Exchange exchange) throws Exception {
        Message message = exchange.getMessage();

        final String body = exchange.getMessage().getBody(String.class);
        if (body != null) {
            Points.PointId id = message.getHeader(Qdrant.Headers.POINT_ID, Points.PointId.class);

            if (id != null) {
                LOG.infof("Processing message with ID: %s", id.getUuid());
            } else {
                id = PointIdFactory.id(UUID.randomUUID());
                LOG.infof("Using the newly created ID: %s", id.getUuid());

                message.setHeader(Qdrant.Headers.POINT_ID, id);
            }
        } else {
            LOG.warnf("Payload body: %s = %s", exchange.getMessage().getBody());
            LOG.warnf("Processing body of type: %s", exchange.getMessage().getBody().getClass());
        }
    }
}
