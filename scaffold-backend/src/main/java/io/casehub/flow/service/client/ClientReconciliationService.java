package io.casehub.flow.service.client;

import io.casehub.flow.service.ReconciliationService;
import io.casehub.flow.service.ReconciliationStatus;
import io.quarkus.arc.DefaultBean;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
@DefaultBean
public class ClientReconciliationService implements ReconciliationService {
    @Override
    public ReconciliationStatus getStatus(UUID applicationId) {
        return new ReconciliationStatus(List.of());
    }

    @Override
    public void triggerReconciliation(UUID applicationId) {}
}
