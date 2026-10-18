package io.casehub.flow.service.client;

import io.casehub.flow.service.DriftReport;
import io.casehub.flow.service.ReconciliationLoopStatus;
import io.casehub.flow.service.ReconciliationService;
import io.quarkus.arc.DefaultBean;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
@DefaultBean
public class ClientReconciliationService implements ReconciliationService {

    @Override
    public List<ReconciliationLoopStatus> activeLoops() {
        return List.of();
    }

    @Override
    public DriftReport getDrift(String loopId) {
        return new DriftReport(loopId, List.of());
    }
}
