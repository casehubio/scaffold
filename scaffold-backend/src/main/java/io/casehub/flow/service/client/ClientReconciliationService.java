package io.casehub.flow.service.client;

import io.casehub.flow.service.ClusterStatus;
import io.casehub.flow.service.ReconciliationService;
import io.casehub.flow.service.ReconciliationStatus;
import io.casehub.flow.service.client.api.OpsReconciliationRestClient;
import io.casehub.flow.service.client.api.OpsReconciliationStatusDto;
import io.quarkus.arc.DefaultBean;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
@DefaultBean
public class ClientReconciliationService implements ReconciliationService {

    @Inject
    @RestClient
    OpsReconciliationRestClient restClient;

    @Override
    public ReconciliationStatus getStatus(UUID applicationId) {
        return toStatus(restClient.getStatus(applicationId));
    }

    @Override
    public void triggerReconciliation(UUID applicationId) {
        restClient.triggerReconciliation(applicationId);
    }

    static ReconciliationStatus toStatus(OpsReconciliationStatusDto dto) {
        if (dto == null) {return new ReconciliationStatus(List.of());}
        return new ReconciliationStatus(
                dto.clusters().stream()
                   .map(c -> new ClusterStatus(c.clusterId(), c.clusterName(),
                                               c.active(), c.nodeCount()))
                   .toList());
    }
}
