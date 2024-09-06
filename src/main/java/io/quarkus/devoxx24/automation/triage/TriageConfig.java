package io.quarkus.devoxx24.automation.triage;

import java.nio.file.Path;
import java.util.Optional;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "devoxx.triage")
public interface TriageConfig {

    @WithDefault("false")
    boolean init();

    Optional<Path> source();
}
