package io.casehub.flow.rest;

import io.casehub.flow.perspective.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class PerspectiveResourceTest {

    @Test
    void returnsPerspectiveWhenPresent() {
        var config = new PerspectiveConfig("ops", "test", "1.0",
            new PerspectiveModule(List.of("engine"), List.of()),
            List.of(new PerspectiveTab("fleet", "Fleet", "fleet-dashboard")),
            List.of(new PerspectiveDataset("deployments", "/api/ops/deployments", "ops-app")));
        var producer = new StubProducer(Optional.of(config));
        var resource = new PerspectiveResource(producer);

        var response = resource.getPerspective();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getEntity()).isInstanceOf(PerspectiveConfig.class);
        var entity = (PerspectiveConfig) response.getEntity();
        assertThat(entity.name()).isEqualTo("ops");
        assertThat(entity.tabs()).hasSize(1);
        assertThat(entity.tabs().get(0).label()).isEqualTo("Fleet");
    }

    @Test
    void returns404WhenNoPerspective() {
        var producer = new StubProducer(Optional.empty());
        var resource = new PerspectiveResource(producer);
        var response = resource.getPerspective();
        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    void classpathPerspectiveServesAllOpsTabs() {
        var config = PerspectiveLoader.loadFromClasspath().orElseThrow();
        var producer = new StubProducer(Optional.of(config));
        var resource = new PerspectiveResource(producer);

        var response = resource.getPerspective();
        assertThat(response.getStatus()).isEqualTo(200);
        var entity = (PerspectiveConfig) response.getEntity();
        assertThat(entity.tabs()).hasSize(6);
        assertThat(entity.tabs().stream().map(PerspectiveTab::id).toList())
            .containsExactly("fleet", "deploy", "health", "drift", "situations", "updates");
        assertThat(entity.datasets()).hasSize(6);
        assertThat(entity.datasets().stream().map(PerspectiveDataset::name).toList())
            .containsExactlyInAnyOrder("applications", "deployments", "clusters",
                "reconciliation", "cases", "approvals");
        assertThat(entity.datasets().stream().map(PerspectiveDataset::endpoint).toList())
            .allMatch(e -> e.startsWith("/api/ops/"));
    }

    static class StubProducer extends PerspectiveProducer {
        private final Optional<PerspectiveConfig> config;
        StubProducer(Optional<PerspectiveConfig> config) { this.config = config; }
        @Override public Optional<PerspectiveConfig> getConfig() { return config; }
    }
}
