package io.casehub.flow.service;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TopologyServiceTest {

    @Test
    void snapshotIsEmptyWhenNoLoopsRunning() {
        TopologyService service = new StubTopologyService();
        var snapshot = service.getSnapshot();
        assertThat(snapshot).isNotNull();
        assertThat(snapshot.loops()).isEmpty();
    }

    static class StubTopologyService implements TopologyService {
        @Override
        public TopologySnapshot getSnapshot() {
            return new TopologySnapshot(List.of());
        }
    }
}
