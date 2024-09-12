package io.quarkus.devoxx24.automation.screenshot;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;
import io.quarkiverse.githubapp.event.Issue;
import io.quarkus.devoxx24.automation.util.Strings;
import jakarta.inject.Inject;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.kohsuke.github.GHEventPayload;

public class ScreenshotListener {

    // Pattern for recognizing a URL, based off RFC 3986
    private static final Pattern urlPattern = Pattern.compile(
            "(?:^|[\\W])((ht|f)tp(s?)://|www\\.)"
            + "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+/?)*"
            + "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]*$~@!:/{};']*)",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
    private static final String GH_ASSETS_PREFIX = "https://github.com/user-attachments/assets";
    private static final String NOT_AN_IMAGE_RESPONSE = "NOT AN IMAGE";

    @Inject
    ChatLanguageModel chatLanguageModel;

    /**
     * Using AI to determine the issue is in English.
     */
    void convertScreenshotsToCode(@Issue.Opened GHEventPayload.Issue issuePayload) throws IOException {
        List<UrlOccurrence> ghScreenshotUrls = new ArrayList<>();
        String body = issuePayload.getIssue().getBody();
        Matcher matcher = urlPattern.matcher(Strings.sanitize(body));
        while (matcher.find()) {
            int matchStart = matcher.start(1);
            int matchEnd = matcher.end() - 1;
            String url = body.substring(matchStart, matchEnd);
            URL repoURL = issuePayload.getRepository().getHtmlUrl();
            if (url.startsWith(GH_ASSETS_PREFIX)) {
                ghScreenshotUrls.add(new UrlOccurrence(matchStart, matchEnd, url));
            }
        }

        if (ghScreenshotUrls.isEmpty()) {
            return;
        }

        Map<UrlOccurrence, String> codeSnippets = new HashMap<>();
        for (UrlOccurrence urlOccurrence : ghScreenshotUrls) {
            UserMessage userMessage = UserMessage.from(
                    TextContent.from(
                            "This is image was reported on a GitHub issue. If this is a snippet of Java code, please respond with only the Java code. If it is not, respond with '"
                            + NOT_AN_IMAGE_RESPONSE + "'"),
                    ImageContent.from(urlOccurrence.url));
            Response<AiMessage> response = chatLanguageModel.generate(userMessage);
            String responseText = response.content().text();
            if (isCodeResponse(responseText)) {
                codeSnippets.put(urlOccurrence, responseText);
            }
        }

        if (codeSnippets.isEmpty()) {
            return;
        }

        StringBuilder commentBuilder = new StringBuilder("Detected the following screenshots that correspond to code:\n\n");
        for (var entry : codeSnippets.entrySet()) {
            commentBuilder.append(entry.getKey().url()).append(" ----> \n").append(entry.getValue()).append("\n\n");
        }

        String comment = commentBuilder.toString();
        if (!comment.isEmpty()) {
            issuePayload.getIssue().comment(commentBuilder.toString());
        }
    }

    private static boolean isCodeResponse(String responseText) {
        if (responseText == null || responseText.isEmpty()) {
            return false;
        }
        return !NOT_AN_IMAGE_RESPONSE.equals(responseText);
    }

    private record UrlOccurrence(int startIndex, int endIndex, String url) {}
}
