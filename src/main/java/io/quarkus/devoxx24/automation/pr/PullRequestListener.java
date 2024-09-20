package io.quarkus.devoxx24.automation.pr;

import io.quarkiverse.githubapp.event.PullRequest;
import jakarta.inject.Inject;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.kohsuke.github.GHEventPayload;
import org.kohsuke.github.GHPullRequestCommitDetail;
import org.kohsuke.github.PagedIterable;

public class PullRequestListener {

    @Inject
    CommitMessageAnalysisAiService commitMessageAnalysisAiService;

    void triagePullRequest(@PullRequest.Opened GHEventPayload.PullRequest pullRequest) throws IOException {
        PagedIterable<GHPullRequestCommitDetail> ghPullRequestCommitDetails =
                pullRequest.getPullRequest().listCommits();

        Map<String, String> suggestions = new LinkedHashMap<>();
        for (var commit : ghPullRequestCommitDetails) {
            String sha = commit.getSha();
            String message = commit.getCommit().getMessage();
            String analysisResult = commitMessageAnalysisAiService.analyze(message);
            if ((analysisResult != null) && !analysisResult.isEmpty() && !analysisResult.equals("ACCEPTED")) {
                suggestions.put(sha, analysisResult);
            }
        }

        if (!suggestions.isEmpty()) {
            StringBuilder comment = new StringBuilder("Thanks you for your Pull Request! We determined that the following commit messages could be improved. Please have a look.\n\n");
            for (var entry : suggestions.entrySet()) {
                comment.append("Commit ").append(entry.getKey()).append(" ->\n").append(entry.getValue()).append("\n\n");
            }
            pullRequest.getPullRequest().comment(comment.toString());
        }
    }

}
