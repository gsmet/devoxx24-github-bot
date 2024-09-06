package io.quarkus.devoxx24.automation.triage;

import java.util.function.Supplier;

import dev.langchain4j.model.embedding.onnx.allminilml6v2q.AllMiniLmL6V2QuantizedEmbeddingModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import io.quarkiverse.langchain4j.pgvector.PgVectorEmbeddingStore;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SimilarityRetrievalAugmentor implements Supplier<RetrievalAugmentor> {

    private final RetrievalAugmentor augmentor;

    SimilarityRetrievalAugmentor(PgVectorEmbeddingStore store, AllMiniLmL6V2QuantizedEmbeddingModel embeddingModel) {
        EmbeddingStoreContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingModel(embeddingModel)
                .embeddingStore(store)
                .maxResults(20)
                .build();
        augmentor = DefaultRetrievalAugmentor
                .builder()
                .contentRetriever(contentRetriever)
                .build();
    }

    @Override
    public RetrievalAugmentor get() {
        return augmentor;
    }
}
