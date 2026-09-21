package io.casehub.flow.rest;

import io.casehub.flow.perspective.PerspectiveConfig;
import io.casehub.flow.perspective.PerspectiveModule;
import io.casehub.flow.perspective.PerspectiveProducer;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ModuleRegistryResourceTest {

    @Test
    void usesPerspectiveDeclaredModulesWhenPresent() {
        var config = new PerspectiveConfig("ops", "test", "1.0",
                new PerspectiveModule(List.of("engine", "work"), List.of()),
                List.of(), List.of());
        var producer = new StubProducer(Optional.of(config));
        var resource = new ModuleRegistryResource(producer);
        var result = resource.getModules();
        assertThat(result.get("modules")).containsExactly("engine", "work");
    }

    @Test
    void fallsBackToClasspathProbingWhenNoPerspective() {
        var producer = new StubProducer(Optional.empty());
        var resource = new ModuleRegistryResource(producer);
        var result = resource.getModules();
        assertThat(result.get("modules")).isNotNull();
    }

    static class StubProducer extends PerspectiveProducer {
        private final Optional<PerspectiveConfig> config;
        StubProducer(Optional<PerspectiveConfig> config) { this.config = config; }
        @Override public Optional<PerspectiveConfig> getConfig() { return config; }
    }
}
