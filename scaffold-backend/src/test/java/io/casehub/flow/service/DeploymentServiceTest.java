package io.casehub.flow.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DeploymentServiceTest {

    @Test
    void availableDefinitionsReturnsEmptyWhenNoneConfigured() {
        DeploymentService service = new StubDeploymentService();
        assertThat(service.availableDefinitions()).isEmpty();
    }

    @Test
    void availableTargetsReturnsEmptyWhenNoneConfigured() {
        DeploymentService service = new StubDeploymentService();
        assertThat(service.availableTargets()).isEmpty();
    }

    static class StubDeploymentService implements DeploymentService {
        @Override
        public List<TopologyDefinition> availableDefinitions() { return List.of(); }

        @Override
        public List<DeploymentTarget> availableTargets() { return List.of(); }

        @Override
        public DeploymentStatus deploy(String definitionId, String targetId) {
            return new DeploymentStatus("none", "PENDING", "");
        }

        @Override
        public DeploymentStatus status(String deploymentId) {
            return new DeploymentStatus(deploymentId, "UNKNOWN", "");
        }
    }
}
