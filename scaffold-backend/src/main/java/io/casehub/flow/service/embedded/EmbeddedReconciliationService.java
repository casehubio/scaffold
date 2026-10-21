package io.casehub.flow.service.embedded;

import io.casehub.flow.service.ClusterStatus;
import io.casehub.flow.service.ReconciliationService;
import io.casehub.flow.service.ReconciliationStatus;
import io.casehub.ops.service.api.OpsReconciliationApi;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
@Alternative
@Priority(100)
public class EmbeddedReconciliationService implements ReconciliationService {

    @Inject
    OpsReconciliationApi reconciliationApi;

    @Override
    public ReconciliationStatus getStatus(UUID applicationId) {
        return toStatus(reconciliationApi.getStatus(applicationId));
    }

    @Override
    public void triggerReconciliation(UUID applicationId) {
        reconciliationApi.triggerReconciliation(applicationId);
    }

    static ReconciliationStatus toStatus(OpsReconciliationApi.ReconciliationStatus ops) {
        if (ops == null) {return new ReconciliationStatus(List.of());}
        return new ReconciliationStatus(
                ops.clusters().stream()
                   .map(c -> new ClusterStatus(c.clusterId(), c.clusterName(),
                                               c.active(), c.nodeCount()))
                   .toList());
    }
}
