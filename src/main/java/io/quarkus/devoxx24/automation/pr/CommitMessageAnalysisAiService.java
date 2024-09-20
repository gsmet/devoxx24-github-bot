package io.quarkus.devoxx24.automation.pr;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;

@RegisterAiService(chatMemoryProviderSupplier = RegisterAiService.NoChatMemoryProviderSupplier.class)
@ApplicationScoped
public interface CommitMessageAnalysisAiService {

    @SystemMessage("You are in charge of making sure commit messages on an open source projects follow the guidelines found in section 5.2 of second edition of the 'Pro Git' book.")
    @UserMessage("""
        The commit to analyze is delimited by --- below.

        ---
        {message}
        ---

        If the commit message does not follow the guidelines and it is possible to extract the meaning of the commit, then kindly suggest how it can rewritten - this suggestion should be surrounded by '```' markdown delimiters.
        If the commit message does not follow the guidelines and is something like 'Fix', 'Polish' or the like, then kindly suggest that the commit needs a more meaningful message.
        If it does follow the guidelines, then simply respond with 'ACCEPTED'.
    """)
    String analyze(String message);
}
