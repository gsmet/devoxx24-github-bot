package io.quarkus.devoxx24.automation.triage;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.kohsuke.github.GHEventPayload;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.onnx.allminilml6v2q.AllMiniLmL6V2QuantizedEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import io.quarkiverse.githubapp.event.Issue;
import io.quarkiverse.langchain4j.pgvector.PgVectorEmbeddingStore;
import io.quarkus.devoxx24.automation.util.Strings;
import jakarta.inject.Inject;

public class TriageIssueListener {

    @Inject
    ObjectMapper objectMapper;

    @Inject
    AllMiniLmL6V2QuantizedEmbeddingModel embeddingModel;

    @Inject
    TriageAiService triageAiService;

    @Inject
    PgVectorEmbeddingStore embeddingStore;

    /**
     * Using AI to determine the best label candidate
     */
    void triageIssueWithLLM(@Issue.Opened GHEventPayload.Issue issuePayload) throws IOException {
        Map<String, Integer> suggestedLabels = triageAiService.suggestLabels(
                Strings.sanitize(issuePayload.getIssue().getTitle()),
                Strings.sanitize(issuePayload.getIssue().getBody()));

        if (suggestedLabels.isEmpty()) {
            return;
        }

        List<String> bestLabels = suggestedLabels.entrySet().stream()
                .filter(e -> e.getValue() >= 8)
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .map(e -> e.getKey())
                .toList();

        if (bestLabels.isEmpty()) {
            return;
        }

        // we only add the best one
        issuePayload.getIssue().addLabels(bestLabels.get(0));
    }

    /**
     * Using pure vector search
     */
    void triageIssueWithVectorSearch(@Issue.Opened GHEventPayload.Issue issuePayload) throws IOException {
        EmbeddingIssue embeddingIssue = new EmbeddingIssue(issuePayload.getIssue().getNumber(), issuePayload.getIssue().getTitle(),
                issuePayload.getIssue().getBody(), List.of());

        String issueJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(embeddingIssue);
        Embedding issueEmbedding = embeddingModel.embed(issueJson).content();

        EmbeddingSearchRequest request = new EmbeddingSearchRequest(issueEmbedding, 20, null, null);
        EmbeddingSearchResult<TextSegment> results = embeddingStore.search(request);

        Map<String, Long> frequency = results.matches().stream()
                .map(em -> em.embedded().text())
                .map(t -> {
                    try {
                        return objectMapper.readValue(t, EmbeddingIssue.class);
                    } catch (JsonProcessingException e) {
                        throw new IllegalStateException(e);
                    }
                })
                .flatMap(i -> i.labels().stream())
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        if (frequency.size() == 0) {
            return;
        }

        Map.Entry<String, Long> bestEntry = frequency.entrySet().iterator().next();
        if (bestEntry.getValue() < 10) {
            return;
        }

        // we only add the best one
        issuePayload.getIssue().addLabels(bestEntry.getKey());
    }
}
