package io.quarkus.devoxx24.automation.triage;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.kohsuke.github.GHEventPayload;

import io.quarkiverse.githubapp.event.Issue;
import jakarta.inject.Inject;

public class TriageIssueListener {

    @Inject
    TriageAiService triageAiService;

    void triageIssue(@Issue.Opened GHEventPayload.Issue issuePayload) throws IOException {
        Map<String, Integer> suggestedLabels = triageAiService.suggestLabels(issuePayload.getIssue().getTitle(),
                issuePayload.getIssue().getBody());

        if (suggestedLabels.isEmpty()) {
            return;
        }

        List<String> bestLabels = suggestedLabels.entrySet().stream()
                .filter(e -> e.getValue() >= 8)
                .sorted((e1, e2) -> -e1.getValue().compareTo(e2.getValue()))
                .map(e -> e.getKey())
                .toList();

        if (bestLabels.isEmpty()) {
            return;
        }

        // we add only the best one
        issuePayload.getIssue().addLabels(bestLabels.get(0));
    }
}
