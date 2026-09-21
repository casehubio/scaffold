package io.casehub.flow.perspective;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PerspectiveLoaderTest {

    @Test
    void loadsValidPerspectiveYaml() {
        var yaml = """
                perspective:
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
        var config = PerspectiveLoader.parse(yaml);
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
    void returnsEmptyForMissingPerspectiveKey() {
        var config = PerspectiveLoader.parse("something: else");
        assertThat(config).isEmpty();
    }

    @Test
    void returnsEmptyForBlankInput() {
        assertThat(PerspectiveLoader.parse("")).isEmpty();
        assertThat(PerspectiveLoader.parse(null)).isEmpty();
    }

    @Test
    void returnsEmptyForInvalidYaml() {
        var config = PerspectiveLoader.parse("not: valid: yaml: {{");
        assertThat(config).isEmpty();
    }

    @Test
    void handlesMinimalPerspective() {
        var yaml = """
                perspective:
                  name: minimal
                """;
        var config = PerspectiveLoader.parse(yaml);
        assertThat(config).isPresent();
        assertThat(config.get().name()).isEqualTo("minimal");
        assertThat(config.get().modules().required()).isEmpty();
        assertThat(config.get().modules().optional()).isEmpty();
        assertThat(config.get().tabs()).isEmpty();
        assertThat(config.get().datasets()).isEmpty();
    }
}
