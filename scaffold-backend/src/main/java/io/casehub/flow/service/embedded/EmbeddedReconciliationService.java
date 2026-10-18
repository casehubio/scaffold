package io.casehub.flow.service.embedded;

import io.casehub.flow.service.DriftReport;
import io.casehub.flow.service.ReconciliationLoopStatus;
import io.casehub.flow.service.ReconciliationService;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;

import java.util.List;

@ApplicationScoped
@Alternative
@Priority(100)
public class EmbeddedReconciliationService implements ReconciliationService {

    @Override
    public List<ReconciliationLoopStatus> activeLoops() {
        return List.of();
    }

    @Override
    public DriftReport getDrift(String loopId) {
        return new DriftReport(loopId, List.of());
    }
}
