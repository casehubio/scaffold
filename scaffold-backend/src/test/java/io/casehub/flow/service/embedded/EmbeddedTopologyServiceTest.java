package io.casehub.flow.service.embedded;

import io.casehub.flow.service.TopologyNode;
import io.casehub.ops.service.api.OpsTopologyApi;
import io.casehub.ops.service.api.OpsTopologyApi.LoopTopology;
import io.casehub.ops.service.api.OpsTopologyApi.NodeStatusEntry;
import io.casehub.ops.service.api.OpsTopologyApi.TopologyAdaptation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EmbeddedTopologyServiceTest {

    @Test
    void flattensLoopNodesIntoFlatList() {
        var opsSnapshot = new OpsTopologyApi.TopologySnapshot(
                List.of(new LoopTopology("tenant:app:cluster",
                        List.of(
                                new OpsTopologyApi.TopologyNode("node-1", "container", "RUNNING", false),
                                new OpsTopologyApi.TopologyNode("node-2", "database", "RUNNING", true)
                        ),
                        List.of(
                                new NodeStatusEntry("node-1", "RUNNING"),
                                new NodeStatusEntry("node-2", "PENDING")
                        )
                )),
                new TopologyAdaptation(List.of(), List.of())
        );

        var service = new EmbeddedTopologyService(opsSnapshot);
        var result = service.getSnapshot("tenant");

        assertThat(result.nodes()).hasSize(2);

        TopologyNode n1 = result.nodes().stream()
                .filter(n -> n.nodeId().equals("node-1")).findFirst().orElseThrow();
        assertThat(n1.nodeType()).isEqualTo("container");
        assertThat(n1.targetStatus()).isEqualTo("RUNNING");
        assertThat(n1.actualStatus()).isEqualTo("RUNNING");
        assertThat(n1.loopKey()).isEqualTo("tenant:app:cluster");
        assertThat(n1.adapted()).isFalse();

        TopologyNode n2 = result.nodes().stream()
                .filter(n -> n.nodeId().equals("node-2")).findFirst().orElseThrow();
        assertThat(n2.actualStatus()).isEqualTo("PENDING");
        assertThat(n2.adapted()).isTrue();
    }

    @Test
    void nodeWithNoActualStatusGetsUnknown() {
        var opsSnapshot = new OpsTopologyApi.TopologySnapshot(
                List.of(new LoopTopology("loop-1",
                        List.of(new OpsTopologyApi.TopologyNode("node-1", "container", "RUNNING", false)),
                        List.of()
                )),
                new TopologyAdaptation(List.of(), List.of())
        );

        var service = new EmbeddedTopologyService(opsSnapshot);
        var result = service.getSnapshot("tenant");

        assertThat(result.nodes()).hasSize(1);
        assertThat(result.nodes().get(0).actualStatus()).isEqualTo("UNKNOWN");
    }
}
