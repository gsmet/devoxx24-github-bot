package io.quarkus.devoxx24.automation.triage;

import java.util.Map;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService(retrievalAugmentor = SimilarityRetrievalAugmentor.class)
public interface TriageAiService {

    @SystemMessage("You are in charge of triaging issues from an Open Source project written in Java.")
    @UserMessage("""
        Find the ten issues represented as JSON documents that are the most similar to the issue delimited by ---

        ---
        {title}

        {body}
        ---

        From now on, only consider these ten most similar issues and ignore the ones that were not selected.
        For these ten most similar issues, return the labels that are affected to them.
        The labels must be returned as a valid JSON map with the key being the label and the value being the number of the ten most similar issues in which this specific label was found.
        Do not return the issues, we only want the labels in the output.
        You must not wrap JSON response in backticks, markdown, or in any other way, but return it as plain text.
    """)
    Map<String, Integer> suggestLabels(String title, String body);
}
