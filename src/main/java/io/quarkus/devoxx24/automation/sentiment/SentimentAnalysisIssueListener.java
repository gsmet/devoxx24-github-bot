package io.quarkus.devoxx24.automation.sentiment;

import java.io.IOException;

import org.kohsuke.github.GHEventPayload;

import io.quarkiverse.githubapp.event.Issue;
import io.quarkiverse.githubapp.event.IssueComment;
import io.quarkus.devoxx24.automation.util.Strings;
import io.quarkus.devoxx24.automation.util.Users;
import jakarta.inject.Inject;

public class SentimentAnalysisIssueListener {

    @Inject
    SentimentAnalysisAiService sentimentAnalysisAiService;

    /**
     * Using AI to determine if an issue body is not following our rules of engagement.
     */
    void warn(@Issue.Opened GHEventPayload.Issue issuePayload) throws IOException {
        String warning = sentimentAnalysisAiService.getWarning(Strings.sanitize(issuePayload.getIssue().getBody()));

        if (warning == null || warning.isBlank()) {
            return;
        }

        issuePayload.getIssue().comment(warning);
    }

    /**
     * Using AI to determine if an issue comment is not following our rules of engagement.
     */
    void warn(@IssueComment.Created GHEventPayload.IssueComment issueCommentPayload) throws IOException {
        if (Users.isBot(issueCommentPayload.getSender().getLogin())) {
            return;
        }

        String warning = sentimentAnalysisAiService.getWarning(Strings.sanitize(issueCommentPayload.getComment().getBody()));

        if (warning == null || warning.isBlank()) {
            return;
        }

        issueCommentPayload.getIssue().comment(warning);
    }
}
