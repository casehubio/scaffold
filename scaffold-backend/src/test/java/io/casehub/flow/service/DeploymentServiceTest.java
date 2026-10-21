package io.casehub.flow.service;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DeploymentServiceTest {

    @Test
    void listDeploymentsReturnsEmptyWhenNoneExist() {
        DeploymentService service = new StubDeploymentService();
        assertThat(service.listDeployments(UUID.randomUUID())).isEmpty();
    }

    @Test
    void deploymentRecordCarriesAllFields() {
        var record = new DeploymentRecord("dep-1", "SUCCESS", "Deployed OK",
                Instant.parse("2026-10-21T00:00:00Z"));
        assertThat(record.deploymentId()).isEqualTo("dep-1");
        assertThat(record.status()).isEqualTo("SUCCESS");
        assertThat(record.message()).isEqualTo("Deployed OK");
        assertThat(record.createdAt()).isNotNull();
    }

    static class StubDeploymentService implements DeploymentService {
        @Override
        public List<DeploymentRecord> listDeployments(UUID applicationId) {
            return List.of();
        }
        @Override
        public DeploymentRecord getCurrentDeployment(UUID applicationId) {
            return null;
        }
        @Override
        public void deploy(UUID applicationId) {}
        @Override
        public void rollback(UUID applicationId) {}
    }
}
