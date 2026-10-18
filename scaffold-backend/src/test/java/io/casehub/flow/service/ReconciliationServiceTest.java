package io.casehub.flow.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReconciliationServiceTest {

    @Test
    void activeLoopsEmptyWhenNoneRunning() {
        ReconciliationService service = new StubReconciliationService();
        assertThat(service.activeLoops()).isEmpty();
    }

    @Test
    void driftReportHasEmptyEntriesForUnknownLoop() {
        ReconciliationService service = new StubReconciliationService();
        var report = service.getDrift("unknown-loop");
        assertThat(report.loopId()).isEqualTo("unknown-loop");
        assertThat(report.entries()).isEmpty();
    }

    static class StubReconciliationService implements ReconciliationService {
        @Override
        public List<ReconciliationLoopStatus> activeLoops() { return List.of(); }

        @Override
        public DriftReport getDrift(String loopId) { return new DriftReport(loopId, List.of()); }
    }
}
