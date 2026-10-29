package io.casehub.flow.rest;

import io.casehub.flow.app.AppConfig;
import io.casehub.flow.app.AppDataset;
import io.casehub.flow.app.AppLoader;
import io.casehub.flow.app.AppModule;
import io.casehub.flow.app.AppProducer;
import io.casehub.flow.app.AppTab;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class AppResourceTest {

    @Test
    void returnsAppWhenPresent() {
        var config = new AppConfig("ops", "test", "1.0",
                                   new AppModule(List.of("engine"), List.of()),
                                   List.of(new AppTab("fleet", "Fleet", "fleet-dashboard")),
                                   List.of(new AppDataset("deployments", "/api/ops/deployments", "ops-app")),
            List.of());
        var producer = new StubProducer(Optional.of(config));
        var resource = new AppResource(producer);

        var response = resource.getApp();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getEntity()).isInstanceOf(AppConfig.class);
        var entity = (AppConfig) response.getEntity();
        assertThat(entity.name()).isEqualTo("ops");
        assertThat(entity.tabs()).hasSize(1);
        assertThat(entity.tabs().get(0).label()).isEqualTo("Fleet");
    }

    @Test
    void returns404WhenNoApp() {
        var producer = new StubProducer(Optional.empty());
        var resource = new AppResource(producer);
        var response = resource.getApp();
        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    void classpathAppServesAllOpsTabs() {
        var config = AppLoader.loadFromClasspath().orElseThrow();
        var producer = new StubProducer(Optional.of(config));
        var resource = new AppResource(producer);

        var response = resource.getApp();
        assertThat(response.getStatus()).isEqualTo(200);
        var entity = (AppConfig) response.getEntity();
        assertThat(entity.tabs()).hasSize(8);
        assertThat(entity.tabs().stream().map(AppTab::id).toList())
            .containsExactly("fleet", "deploy", "health", "drift", "situations", "topology", "updates", "cases");
        assertThat(entity.datasets()).hasSize(10);
        assertThat(entity.datasets().stream().map(AppDataset::name).toList())
            .containsExactlyInAnyOrder("applications", "deployments", "clusters",
                "reconciliation", "cases", "approvals", "topology",
                "scaffold-cases", "scaffold-approvals", "catalogue");
        assertThat(entity.datasets().stream().map(AppDataset::endpoint).toList())
            .allMatch(e -> e.startsWith("/api/ops/") || e.startsWith("/api/scaffold/"));
    }

    static class StubProducer extends AppProducer {
        private final Optional<AppConfig> config;
        StubProducer(Optional<AppConfig> config)         {this.config = config; }
        @Override public Optional<AppConfig> getConfig() { return config; }
    }
}
