package com.github.lburgazzoli.camel.ai.ingestion;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import io.qdrant.client.PointIdFactory;
import io.qdrant.client.ValueFactory;
import io.qdrant.client.VectorsFactory;
import io.qdrant.client.grpc.Points;
import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.apache.camel.component.langchain.embeddings.LangchainEmbeddings;

import java.util.UUID;

public class AsPointStruct {
    public static final String POINT_ID_HEADER = "point.id";

    @Handler
    public Points.PointStruct asPointStruct(Exchange e) {
        Embedding embedding = e.getMessage().getHeader(LangchainEmbeddings.Headers.VECTOR, Embedding.class);
        TextSegment text = e.getMessage().getBody(TextSegment.class);
        Points.PointId id = e.getMessage().getHeader(POINT_ID_HEADER, () -> PointIdFactory.id(UUID.randomUUID()), Points.PointId.class);

        var builder = Points.PointStruct.newBuilder();
        builder.setId(id);
        builder.setVectors(VectorsFactory.vectors(embedding.vector()));

        Points.PointId.newBuilder().setUuid(id.toString()).build();

        if (text != null) {
            // this is the default for loangchain4j
            // https://github.com/langchain4j/langchain4j/blob/3e432486ffc5cb80861e118cbc974f478c3accfc/langchain4j-qdrant/src/main/java/dev/langchain4j/store/embedding/qdrant/QdrantEmbeddingStore.java#L261
            builder.putPayload("text_segment", ValueFactory.value(text.text()));

            text.metadata()
                    .asMap()
                    .forEach((key, value) -> builder.putPayload(key, ValueFactory.value(value)));
        }

        return builder.build();
    }
}
