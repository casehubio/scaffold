package io.casehub.flow.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TopologyServiceTest {

    @Test
    void snapshotIsEmptyWhenNoNodesPresent() {
        TopologyService service = tenancyId -> new TopologySnapshot(List.of());
        var snapshot = service.getSnapshot("tenant-1");
        assertThat(snapshot).isNotNull();
        assertThat(snapshot.nodes()).isEmpty();
    }

    @Test
    void snapshotContainsNodesWithAllFields() {
        var node = new TopologyNode("node-1", "container", "RUNNING",
                "RUNNING", "loop-key-1", false);
        TopologyService service = tenancyId -> new TopologySnapshot(List.of(node));
        var snapshot = service.getSnapshot("tenant-1");
        assertThat(snapshot.nodes()).hasSize(1);
        var n = snapshot.nodes().get(0);
        assertThat(n.nodeId()).isEqualTo("node-1");
        assertThat(n.nodeType()).isEqualTo("container");
        assertThat(n.targetStatus()).isEqualTo("RUNNING");
        assertThat(n.actualStatus()).isEqualTo("RUNNING");
        assertThat(n.loopKey()).isEqualTo("loop-key-1");
        assertThat(n.adapted()).isFalse();
    }
}
