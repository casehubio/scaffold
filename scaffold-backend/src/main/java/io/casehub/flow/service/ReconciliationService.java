package io.casehub.flow.service;

import java.util.List;

public interface ReconciliationService {
    List<ReconciliationLoopStatus> activeLoops();
    DriftReport getDrift(String loopId);
}
