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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;
import org.apache.camel.assistant.main.common.AlpacaRecord;
import org.apache.camel.assistant.main.consume.ConsumeService;
import picocli.CommandLine;

@CommandLine.Command(name = "dataset",
        description = "Consume a new dynamic or static knowledge", sortOptions = false)
public class ConsumeDataSet extends BaseCommand {

    @CommandLine.Option(names = {"--source"}, description = "The source of information", arity = "0..1")
    private String source;

    @CommandLine.Option(names = {"--id"}, description = "The ID of the information from the source", arity = "0..1")
    private String id;

    @CommandLine.Option(names = {"--path"}, description = "The path to the dataset to load", arity = "0..1")
    private String path;

    @CommandLine.Option(names = {"--address"}, description = "The service address", arity = "0..1", required = true, defaultValue = "http://localhost:8083")
    private String address;

    ConsumeService learnService;

    @Override
    public void run() {
        learnService = QuarkusRestClientBuilder.newBuilder()
                .baseUri(URI.create(address))
                .build(ConsumeService.class);

        File datasetDir = new File(path);
        if (datasetDir.isDirectory()) {
            final File[] files = datasetDir.listFiles();
            if (files == null) {
                System.out.println("There are no files in " + datasetDir.getAbsolutePath());
                return;
            }

            for (int i = 0; i < files.length; i++) {
                final File file = files[i];
                if (!file.getName().endsWith(".json")) {
                    System.err.printf("Skipping file %d of %d %s: it's not a dataset file%n", i, files.length, file);
                }

                System.err.printf("Loading dataset file %d of %d %s%n", i, files.length, file);
                try (InputStream stream = new BufferedInputStream(new FileInputStream(file))) {
                    ObjectMapper mapper = new ObjectMapper();
                    final AlpacaRecord[] alpacaRecords = mapper.readValue(stream, AlpacaRecord[].class);
                    for (AlpacaRecord record : alpacaRecords) {
                        learnService.consumeTextStatic(record.getOutput());
                    }
                } catch (FileNotFoundException e) {
                    System.err.printf("File %s not found%n [Skipping]", file);
                } catch (IOException e) {
                    System.err.printf("Unhandled I/O error '%s' processing %s%n [Skipping]", e.getMessage(), file);
                }
            }
        }
    }
}
