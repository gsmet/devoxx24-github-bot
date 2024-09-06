package io.quarkus.devoxx24.automation.triage;

import java.util.List;

public record EmbeddingIssue(int number, String title, String body, List<String> labels) {

}
