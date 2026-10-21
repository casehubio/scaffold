package io.casehub.flow.service.embedded;

import io.casehub.flow.service.TopologyNode;
import io.casehub.flow.service.TopologyService;
import io.casehub.flow.service.TopologySnapshot;
import io.casehub.ops.service.api.OpsTopologyApi;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
@Alternative
@Priority(100)
public class EmbeddedTopologyService implements TopologyService {

    private final OpsTopologyApi                  topologyApi;
    private final OpsTopologyApi.TopologySnapshot testSnapshot;

    @Inject
    public EmbeddedTopologyService(OpsTopologyApi topologyApi) {
        this.topologyApi  = topologyApi;
        this.testSnapshot = null;
    }

    EmbeddedTopologyService(OpsTopologyApi.TopologySnapshot testSnapshot) {
        this.topologyApi  = null;
        this.testSnapshot = testSnapshot;
    }

    @Override
    public TopologySnapshot getSnapshot(String tenancyId) {
        var opsSnapshot = testSnapshot != null
                          ? testSnapshot
                          : topologyApi.getTopology(tenancyId);

        List<TopologyNode> nodes = new ArrayList<>();
        for (var loop : opsSnapshot.loops()) {
            Map<String, String> actualMap = loop.actualStatuses().stream()
                                                .collect(Collectors.toMap(
                                                        OpsTopologyApi.NodeStatusEntry::nodeId,
                                                        OpsTopologyApi.NodeStatusEntry::status));

            for (var desired : loop.desiredNodes()) {
                String actualStatus = actualMap.getOrDefault(desired.nodeId(), "UNKNOWN");
                nodes.add(new TopologyNode(
                        desired.nodeId(),
                        desired.nodeType(),
                        desired.targetStatus(),
                        actualStatus,
                        loop.loopKey(),
                        desired.adapted()));
            }
        }
        return new TopologySnapshot(nodes);
    }
}
