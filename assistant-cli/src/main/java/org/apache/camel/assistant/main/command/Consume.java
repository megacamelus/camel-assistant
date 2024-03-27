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

package org.apache.camel.assistant.main.command;

import java.net.URI;

import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;
import org.apache.camel.assistant.main.consume.ConsumeService;
import picocli.CommandLine;

@CommandLine.Command(name = "Consume",
        description = "Consume a new dynamic or static knowledge", sortOptions = false)
public class Consume extends BaseCommand {

    @CommandLine.Option(names = {"-d", "--dynamic"}, description = "Whether it is using static or dynamic information")
    private boolean dynamic;

    @CommandLine.Option(names = {"--source"}, description = "The source of information", arity = "0..1")
    private String source;

    @CommandLine.Option(names = {"--id"}, description = "The ID of the information from the source", arity = "0..1")
    private String id;

    @CommandLine.Option(names = {"--data"}, description = "The data to load", arity = "0..1", required = true)
    private String data;

    @CommandLine.Option(names = {"--address"}, description = "The service address", arity = "0..1", required = true, defaultValue = "http://localhost:8083")
    private String address;

    ConsumeService learnService;

    @Override
    public void run() {
        learnService = QuarkusRestClientBuilder.newBuilder()
                .baseUri(URI.create(address))
                .build(ConsumeService.class);

        if (dynamic) {
            learnService.consumeDynamic(source, id, data);
        } else {
            learnService.consumeStatic(data);
        }



    }
}
