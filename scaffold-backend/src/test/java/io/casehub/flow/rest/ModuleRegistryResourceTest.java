package io.casehub.flow.rest;

import io.casehub.flow.app.AppConfig;
import io.casehub.flow.app.AppModule;
import io.casehub.flow.app.AppProducer;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ModuleRegistryResourceTest {

    @Test
    void usesAppDeclaredModulesWhenPresent() {
        var config = new AppConfig("ops", "test", "1.0",
                                   new AppModule(List.of("engine", "work"), List.of()),
                                   List.of(), List.of(), List.of());
        var producer = new StubProducer(Optional.of(config));
        var resource = new ModuleRegistryResource(producer);
        var result = resource.getModules();
        assertThat(result.get("modules")).containsExactly("engine", "work");
    }

    @Test
    void fallsBackToClasspathProbingWhenNoApp() {
        var producer = new StubProducer(Optional.empty());
        var resource = new ModuleRegistryResource(producer);
        var result = resource.getModules();
        assertThat(result.get("modules")).isNotNull();
    }

    @Test
    void detectsOpsServiceAsOptionalModule() {
        var config = new AppConfig("ops", "test", "1.0",
                                   new AppModule(List.of("engine"), List.of("ops-service")),
                                   List.of(), List.of(), List.of());
        var producer = new StubProducer(Optional.of(config));
        var resource = new ModuleRegistryResource(producer);
        var result   = resource.getModules();
        assertThat(result.get("modules")).contains("engine", "ops-service");
    }


    static class StubProducer extends AppProducer {
        private final Optional<AppConfig> config;
        StubProducer(Optional<AppConfig> config)         {this.config = config; }
        @Override public Optional<AppConfig> getConfig() { return config; }
    }
}
