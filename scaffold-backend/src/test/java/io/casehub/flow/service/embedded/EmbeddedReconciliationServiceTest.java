package io.casehub.flow.service.embedded;

import io.casehub.ops.service.api.OpsReconciliationApi;
import io.casehub.ops.service.api.OpsReconciliationApi.ClusterReconciliationStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EmbeddedReconciliationServiceTest {

    @Test
    void mapsOpsStatusToScaffoldStatus() {
        var opsStatus = new OpsReconciliationApi.ReconciliationStatus(List.of(
                new ClusterReconciliationStatus("c-1", "prod", true, 5),
                new ClusterReconciliationStatus("c-2", "staging", false, 0)
        ));

        var result = EmbeddedReconciliationService.toStatus(opsStatus);
        assertThat(result.clusters()).hasSize(2);
        assertThat(result.clusters().get(0).clusterId()).isEqualTo("c-1");
        assertThat(result.clusters().get(0).active()).isTrue();
        assertThat(result.clusters().get(1).active()).isFalse();
    }

    @Test
    void mapsNullStatusToEmptyClusters() {
        var result = EmbeddedReconciliationService.toStatus(null);
        assertThat(result.clusters()).isEmpty();
    }
}
