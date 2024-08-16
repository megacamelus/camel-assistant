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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.function.Consumer;

import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;
import org.apache.camel.assistant.main.consume.ConsumeService;
import picocli.CommandLine;

@CommandLine.Command(name = "file",
        description = "Consume a new dynamic or static knowledge", sortOptions = false)
public class ConsumeFile extends BaseCommand {

    @CommandLine.Option(names = {"-d", "--dynamic"}, description = "Whether it is using static or dynamic information")
    private boolean dynamic;

    @CommandLine.Option(names = {"--source"}, description = "The source of information", arity = "0..1")
    private String source;

    @CommandLine.Option(names = {"--id"}, description = "The ID of the information from the source", arity = "0..1")
    private String id;

    @CommandLine.Parameters(description = "The file to load", arity = "1..1")
    private String file;

    @CommandLine.Option(names = {"--address"}, description = "The service address", arity = "0..1", required = true, defaultValue = "http://localhost:8083")
    private String address;

    @CommandLine.Option(names = {"--remove-pages"}, description = "An optional list of pages to remove (in the format N-N,N - ex.: 1-2,4)", arity = "0..1")
    private String removePages;

    @CommandLine.Option(names = {"--splitter-name"}, description = "To use an specific document splitter (overrides the server one)", arity = "0..1")
    private String splitterName;

    ConsumeService consumeService;

    @Override
    public void run() {
        consumeService = QuarkusRestClientBuilder.newBuilder()
                .baseUri(URI.create(address))
                .build(ConsumeService.class);

        if (file.endsWith(".pdf")) {
            consumeFile(b -> consumePdf(b));
        } else {
            consumeFile(b -> consumeText(b));
        }
    }

    private void consumeText(byte[] data) {
        final String content = new String(data);

        consumeService.consumeFileStatic(splitterName, content);
    }

    private void consumePdf(byte[] data) {
        consumeService.consumePdfStatic(removePages, splitterName, data);
    }

    private void consumeFile(Consumer<byte[]> consumer) {
        try (InputStream stream = new BufferedInputStream(new FileInputStream(file))) {
            final byte[] data = stream.readAllBytes();

            consumer.accept(data);
        } catch (FileNotFoundException e) {
            System.err.printf("Cannot process file %s: file does not exist%n", file);
            System.exit(1);
        } catch (IOException e) {
            System.err.printf("Unhandled I/O error processing file %s: %s%n", file, e.getMessage());
            System.exit(1);
        }
    }
}
