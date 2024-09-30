package io.quarkus.devoxx24.automation.language;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService
public interface LanguageDetectorAiService {

    @SystemMessage("You are in charge of detecting the language used in a text.")
    @UserMessage("""
        If the text delimited by --- is in English, return true. If it is not in English, return false.

        ---
        {title}

        {body}
        ---

        You output the result in plain text. Do not format it.
    """)
    boolean isEnglish(String title, String body);
}
