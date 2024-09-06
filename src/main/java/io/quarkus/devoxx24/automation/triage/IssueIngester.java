package io.quarkus.devoxx24.automation.triage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.jboss.logging.Logger;
import org.kohsuke.github.GHIssue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.model.embedding.onnx.allminilml6v2q.AllMiniLmL6V2QuantizedEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import io.quarkiverse.langchain4j.pgvector.PgVectorEmbeddingStore;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class IssueIngester {

    private static final Logger LOG = Logger.getLogger(IssueIngester.class);

    @Inject
    TriageConfig triageConfig;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    AllMiniLmL6V2QuantizedEmbeddingModel embeddingModel;

    @Inject
    PgVectorEmbeddingStore store;

    void init(@Observes StartupEvent startupEvent) {
        if (!triageConfig.init() || triageConfig.source().isEmpty()) {
            return;
        }

        if (!Files.isDirectory(triageConfig.source().get())) {
            throw new IllegalArgumentException("Path " + triageConfig.source().get() + " is not a directory");
        }

        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .embeddingStore(store)
                .embeddingModel(embeddingModel)
                .build();

        final ObjectWriter objectWriter = objectMapper.writerWithDefaultPrettyPrinter();

        try (Stream<Path> jsonFiles = Files.list(triageConfig.source().get()).sorted()) {
            jsonFiles.forEach(p -> {
                if (!p.getFileName().toString().endsWith(".json")) {
                    return;
                }

                try {
                    GHIssue issue = Utils.getGitHubObjectReader().readValue(p.toFile(), GHIssue.class);

                    EmbeddingIssue embeddingIssue = new EmbeddingIssue(issue.getNumber(), issue.getTitle(), issue.getBody(),
                            issue.getLabels().stream().map(l -> l.getName()).filter(n -> n.startsWith("area/")).toList());

                    if (embeddingIssue.labels().isEmpty()) {
                        return;
                    }

                    Metadata metadata = new Metadata();
                    metadata.put("type", "issue");
                    metadata.put("number", embeddingIssue.number());
                    metadata.put("title", embeddingIssue.title());
                    Document document = new Document(objectWriter.writeValueAsString(embeddingIssue), metadata);

                    LOG.info("Ingesting document for issue #" + embeddingIssue.number() + " - " + embeddingIssue.title());
                    ingestor.ingest(document);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            throw new IllegalStateException("Unable to list files from " + triageConfig.source().get());
        }
    }
}
