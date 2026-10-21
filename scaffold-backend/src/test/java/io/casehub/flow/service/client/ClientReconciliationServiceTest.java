package io.casehub.flow.service.client;

import io.casehub.flow.service.client.api.OpsClusterStatusDto;
import io.casehub.flow.service.client.api.OpsReconciliationStatusDto;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ClientReconciliationServiceTest {

    @Test
    void mapsRestDtoToReconciliationStatus() {
        var dto = new OpsReconciliationStatusDto(List.of(
                new OpsClusterStatusDto("c-1", "prod", true, 3)));
        var status = ClientReconciliationService.toStatus(dto);
        assertThat(status.clusters()).hasSize(1);
        assertThat(status.clusters().get(0).clusterId()).isEqualTo("c-1");
    }

    @Test
    void mapsNullToEmptyStatus() {
        var status = ClientReconciliationService.toStatus(null);
        assertThat(status.clusters()).isEmpty();
    }
}
