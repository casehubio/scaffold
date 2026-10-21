package io.casehub.flow.service;

import java.util.UUID;

public interface ReconciliationService {
    ReconciliationStatus getStatus(UUID applicationId);

    void triggerReconciliation(UUID applicationId);
}
