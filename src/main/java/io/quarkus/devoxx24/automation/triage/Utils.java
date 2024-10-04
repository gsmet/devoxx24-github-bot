package io.quarkus.devoxx24.automation.triage;

import org.kohsuke.github.GitHub;
import org.kohsuke.github.connector.GitHubConnectorResponse;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;

public final class Utils {

    private Utils() {
    }

    /**
     * @return a carefully crafted ObjectReader to be able to use the Hub4j GitHub API outside of the GitHub API infrastructure.
     */
    static ObjectReader getGitHubObjectReader() {
        final ObjectMapper gitHubObjectMapper = new ObjectMapper();
        gitHubObjectMapper.setVisibility(
                new VisibilityChecker.Std(Visibility.NONE, Visibility.NONE, Visibility.NONE, Visibility.NONE, Visibility.ANY));
        gitHubObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        gitHubObjectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true);
        gitHubObjectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

        InjectableValues.Std inject = new InjectableValues.Std();
        inject.addValue(GitHub.class, null);
        inject.addValue(GitHubConnectorResponse.class, null);

        return gitHubObjectMapper.reader(inject);
    }
}
