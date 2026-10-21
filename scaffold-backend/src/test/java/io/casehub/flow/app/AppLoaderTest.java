package io.casehub.flow.app;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AppLoaderTest {

    @Test
    void loadsValidAppYaml() {
        var yaml = """
                app:
                  name: ops
                  description: "CaseHub operations management"
                  version: "1.0"
                  modules:
                    required: [engine, desiredstate, ops-deployment, work]
                    optional: [ops-compliance]
                  views:
                    tabs:
                      - id: fleet
                        label: "Fleet"
                        view: fleet-dashboard
                      - id: deploy
                        label: "Deploy"
                        view: deployment-wizard
                  datasets:
                    deployments:
                      endpoint: /api/ops/deployments
                      source: ops-app
                """;
        var config = AppLoader.parse(yaml);
        assertThat(config).isPresent();
        assertThat(config.get().name()).isEqualTo("ops");
        assertThat(config.get().modules().required()).containsExactly("engine", "desiredstate", "ops-deployment", "work");
        assertThat(config.get().modules().optional()).containsExactly("ops-compliance");
        assertThat(config.get().tabs()).hasSize(2);
        assertThat(config.get().tabs().get(0).id()).isEqualTo("fleet");
        assertThat(config.get().tabs().get(0).label()).isEqualTo("Fleet");
        assertThat(config.get().tabs().get(0).view()).isEqualTo("fleet-dashboard");
        assertThat(config.get().datasets()).hasSize(1);
        assertThat(config.get().datasets().get(0).name()).isEqualTo("deployments");
        assertThat(config.get().datasets().get(0).endpoint()).isEqualTo("/api/ops/deployments");
    }

    @Test
    void returnsEmptyForMissingAppKey() {
        var config = AppLoader.parse("something: else");
        assertThat(config).isEmpty();
    }

    @Test
    void returnsEmptyForBlankInput() {
        assertThat(AppLoader.parse("")).isEmpty();
        assertThat(AppLoader.parse(null)).isEmpty();
    }

    @Test
    void returnsEmptyForInvalidYaml() {
        var config = AppLoader.parse("not: valid: yaml: {{");
        assertThat(config).isEmpty();
    }

    @Test
    void classpathAppHasAllOpsDatasets() {
        var config = AppLoader.loadFromClasspath().orElseThrow();
        var names = config.datasets().stream().map(AppDataset::name).toList();
        assertThat(names).containsExactlyInAnyOrder(
            "applications", "deployments", "clusters", "reconciliation", "cases", "approvals", "topology");

        var endpoints = config.datasets().stream().map(AppDataset::endpoint).toList();
        assertThat(endpoints).allMatch(e -> e.startsWith("/api/ops/") || e.startsWith("/api/scaffold/"));
    }

    @Test
    void handlesMinimalApp() {
        var yaml = """
                app:
                  name: minimal
                """;
        var config = AppLoader.parse(yaml);
        assertThat(config).isPresent();
        assertThat(config.get().name()).isEqualTo("minimal");
        assertThat(config.get().modules().required()).isEmpty();
        assertThat(config.get().modules().optional()).isEmpty();
        assertThat(config.get().tabs()).isEmpty();
        assertThat(config.get().datasets()).isEmpty();
    }

    @Test
    void parsesConnectionsSection() {
        var yaml = """
                   app:
                     name: ops
                     connections:
                       reconciliation-events:
                         endpoint: /api/ops/reconciliation/events
                         type: sse
                         source: reconciliation-service
                   """;
        var config = AppLoader.parse(yaml);
        assertThat(config).isPresent();
        assertThat(config.get().connections()).hasSize(1);
        var conn = config.get().connections().get(0);
        assertThat(conn.name()).isEqualTo("reconciliation-events");
        assertThat(conn.endpoint()).isEqualTo("/api/ops/reconciliation/events");
        assertThat(conn.type()).isEqualTo("sse");
        assertThat(conn.source()).isEqualTo("reconciliation-service");
    }

    @Test
    void emptyConnectionsWhenSectionMissing() {
        var yaml = """
                   app:
                     name: minimal
                   """;
        var config = AppLoader.parse(yaml);
        assertThat(config).isPresent();
        assertThat(config.get().connections()).isEmpty();
    }


}
