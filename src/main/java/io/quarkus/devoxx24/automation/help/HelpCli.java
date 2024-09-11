package io.quarkus.devoxx24.automation.help;

import java.io.IOException;
import java.util.List;

import org.kohsuke.github.GHEventPayload;
import org.kohsuke.github.GHEventPayload.IssueComment;
import org.kohsuke.github.GHPermissionType;

import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Cli;
import com.github.rvesse.airline.annotations.Command;

import io.quarkiverse.githubapp.command.airline.Permission;
import io.quarkus.devoxx24.automation.help.HelpCli.Explain;
import io.quarkus.devoxx24.automation.util.Strings;
import jakarta.inject.Inject;

@Cli(name = "@bot", commands = { Explain.class })
public class HelpCli {

    @Command(name = "explain")
    @Permission(GHPermissionType.WRITE)
    static class Explain implements Commands {

        @Inject
        HelpAiService helpAiService;

        @Arguments
        List<String> arguments;

        @Override
        public void run(IssueComment issueCommentPayload) throws IOException {
            if (arguments == null || arguments.isEmpty()) {
                return;
            }

            String help = helpAiService.explain(Strings.sanitize(String.join(" ", arguments)));
            if (help == null || help.isBlank()) {
                return;
            }

            issueCommentPayload.getIssue().comment(help);
        }
    }

    interface Commands {

        void run(GHEventPayload.IssueComment issueCommentPayload) throws IOException;
    }
}
