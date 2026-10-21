package io.casehub.flow.service.client;

import io.casehub.flow.service.client.api.OpsDeploymentRecordDto;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class ClientDeploymentServiceTest {

    @Test
    void mapsRestDtoToDeploymentRecord() {
        var dto = new OpsDeploymentRecordDto("dep-1", "SUCCESS",
                Instant.parse("2026-10-20T10:00:00Z"));
        var record = ClientDeploymentService.toRecord(dto);
        assertThat(record.deploymentId()).isEqualTo("dep-1");
        assertThat(record.status()).isEqualTo("SUCCESS");
        assertThat(record.createdAt()).isNotNull();
    }
}
