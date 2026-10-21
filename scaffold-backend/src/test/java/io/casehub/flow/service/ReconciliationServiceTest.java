package io.casehub.flow.service;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ReconciliationServiceTest {

    @Test
    void statusHasEmptyClustersWhenNoneActive() {
        ReconciliationService service = new StubReconciliationService();
        var status = service.getStatus(UUID.randomUUID());
        assertThat(status.clusters()).isEmpty();
    }

    @Test
    void clusterStatusCarriesAllFields() {
        var cluster = new ClusterStatus("c-1", "prod-cluster", true, 5);
        assertThat(cluster.clusterId()).isEqualTo("c-1");
        assertThat(cluster.clusterName()).isEqualTo("prod-cluster");
        assertThat(cluster.active()).isTrue();
        assertThat(cluster.nodeCount()).isEqualTo(5);
    }

    static class StubReconciliationService implements ReconciliationService {
        @Override
        public ReconciliationStatus getStatus(UUID applicationId) {
            return new ReconciliationStatus(List.of());
        }
        @Override
        public void triggerReconciliation(UUID applicationId) {}
    }
}
