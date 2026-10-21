package io.casehub.flow.service.embedded;

import io.casehub.flow.service.ReconciliationService;
import io.casehub.flow.service.ReconciliationStatus;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
@Alternative
@Priority(100)
public class EmbeddedReconciliationService implements ReconciliationService {
    @Override
    public ReconciliationStatus getStatus(UUID applicationId) {
        return new ReconciliationStatus(List.of());
    }

    @Override
    public void triggerReconciliation(UUID applicationId) {}
}
