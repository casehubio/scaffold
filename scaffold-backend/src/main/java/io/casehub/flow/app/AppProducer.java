package io.casehub.flow.app;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

@ApplicationScoped
public class AppProducer {

    private final Optional<AppConfig> config = AppLoader.loadFromClasspath();

    public Optional<AppConfig> getConfig() {
        return config;
    }
}
