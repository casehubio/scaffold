package io.casehub.flow.service.embedded;

import io.casehub.ops.service.entity.DeploymentRecordEntity;
import io.casehub.ops.service.model.DeploymentOutcome;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EmbeddedDeploymentServiceTest {

    @Test
    void mapsEntityFieldsToRecord() {
        var entity = new DeploymentRecordEntity();
        entity.id = UUID.randomUUID();
        entity.outcome = DeploymentOutcome.SUCCESS;
        entity.createdAt = Instant.parse("2026-10-20T10:00:00Z");

        var record = EmbeddedDeploymentService.toRecord(entity);
        assertThat(record.deploymentId()).isEqualTo(entity.id.toString());
        assertThat(record.status()).isEqualTo("SUCCESS");
        assertThat(record.createdAt()).isEqualTo(entity.createdAt);
    }

    @Test
    void mapsNullOutcomeToUnknown() {
        var entity = new DeploymentRecordEntity();
        entity.id = UUID.randomUUID();
        entity.outcome = null;
        entity.createdAt = Instant.now();

        var record = EmbeddedDeploymentService.toRecord(entity);
        assertThat(record.status()).isEqualTo("UNKNOWN");
    }
}
