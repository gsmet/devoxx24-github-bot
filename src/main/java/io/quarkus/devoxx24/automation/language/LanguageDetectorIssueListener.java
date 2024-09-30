package io.quarkus.devoxx24.automation.language;

import java.io.IOException;

import org.kohsuke.github.GHEventPayload;

import io.quarkiverse.githubapp.event.Issue;
import io.quarkus.devoxx24.automation.util.Strings;
import jakarta.inject.Inject;

public class LanguageDetectorIssueListener {

    @Inject
    LanguageDetectorAiService languageDetectorAiService;

    /**
     * Using AI to determine the issue is in English.
     */
    void detectLanguage(@Issue.Opened GHEventPayload.Issue issuePayload) throws IOException {
        if (languageDetectorAiService.isEnglish(
                Strings.sanitize(issuePayload.getIssue().getTitle()),
                Strings.sanitize(issuePayload.getIssue().getBody()))) {
            return;
        }

        issuePayload.getIssue().comment(
                "We value our users from all around the world but please create your issues in English as this is the common language used by the team. Thanks!");
        issuePayload.getIssue().close();
    }
}
