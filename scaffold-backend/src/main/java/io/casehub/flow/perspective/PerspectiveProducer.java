package io.casehub.flow.perspective;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

@ApplicationScoped
public class PerspectiveProducer {

    private final Optional<PerspectiveConfig> config = PerspectiveLoader.loadFromClasspath();

    public Optional<PerspectiveConfig> getConfig() {
        return config;
    }
}
