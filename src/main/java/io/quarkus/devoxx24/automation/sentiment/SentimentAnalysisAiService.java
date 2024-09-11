package io.quarkus.devoxx24.automation.sentiment;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService
public interface SentimentAnalysisAiService {

    @SystemMessage("You are a community moderator. Your role is to analyze comments to make sure people stay civil and our community is inclusive.")
    @UserMessage("""
        The comment to analyze is delimited by --- below.
        If the comment to analyze is fine, return an empty string and only an empty string.
        If the comment to analyze is aggressive, excessively sarcastic or contains racism or degrading comments, return a polite and concise message as to why this comment is not acceptable.

        ---
        {comment}
        ---

        You output the result as plain text. Do not format it.
    """)
    String getWarning(String comment);
}
