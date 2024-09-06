package io.quarkus.devoxx24.automation.triage;

import java.util.List;
import java.util.Map;

import org.jboss.resteasy.reactive.RestQuery;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.onnx.allminilml6v2q.AllMiniLmL6V2QuantizedEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import io.quarkiverse.langchain4j.pgvector.PgVectorEmbeddingStore;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("triage")
public class TriageResource {

    @Inject
    ObjectMapper objectMapper;

    @Inject
    AllMiniLmL6V2QuantizedEmbeddingModel embeddingModel;

    @Inject
    PgVectorEmbeddingStore embeddingStore;

    @Inject
    TriageAiService triageAiService;

    @GET
    @Path("issues")
    public List<EmbeddingIssue> similarIssues(@RestQuery String title, @RestQuery String body) throws JsonProcessingException {
        EmbeddingIssue embeddingIssue = new EmbeddingIssue(0, title, body, null);

        String issueJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(embeddingIssue);
        Embedding issueEmbedding = embeddingModel.embed(issueJson).content();

        EmbeddingSearchRequest request = new EmbeddingSearchRequest(issueEmbedding, 20, null, null);
        EmbeddingSearchResult<TextSegment> results = embeddingStore.search(request);

        return results.matches().stream()
                .map(em -> em.embedded().text())
                .map(t -> {
                    try {
                        return objectMapper.readValue(t, EmbeddingIssue.class);
                    } catch (JsonProcessingException e) {
                        throw new IllegalStateException(e);
                    }
                })
                .toList();
    }

    @GET
    @Path("labels")
    public Map<String, Integer> labels(@RestQuery String title, @RestQuery String body) throws JsonProcessingException {
        return triageAiService.suggestLabels(title, body);
    }
}
