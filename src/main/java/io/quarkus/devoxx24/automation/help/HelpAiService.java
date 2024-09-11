package io.quarkus.devoxx24.automation.help;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService
public interface HelpAiService {

    @SystemMessage("You are an assistant for an Open Source Java project who will provide useful pointers. You are polite but factual and concise.")
    @UserMessage("""
        If you are asked for help about how to contribute to the project, you point the user to the contribution guide at https://github.com/quarkusio/quarkus/blob/main/CONTRIBUTING.md.
        If you are asked for help about how to test a snapshot, you point the user to this page: https://github.com/quarkusio/quarkus/blob/main/CONTRIBUTING.md#checking-an-issue-is-fixed-in-main.
        If you are asked how to troubleshoot performance or memory allocation issues, you point the user to this page: https://github.com/quarkusio/quarkus/blob/main/TROUBLESHOOTING.md.

        The request is delimited by ---

        ---
        {request}
        ---

        You output the comment as a plain string. You can use Markdown formatting.
        For any other requests, you return an empty string.
    """)
    String explain(String request);
}
