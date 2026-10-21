package io.casehub.flow.service.client;

import io.casehub.flow.service.TopologyNode;
import io.casehub.flow.service.TopologyService;
import io.casehub.flow.service.TopologySnapshot;
import io.casehub.flow.service.client.api.OpsNodeStatusDto;
import io.casehub.flow.service.client.api.OpsTopologyRestClient;
import io.quarkus.arc.DefaultBean;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
@DefaultBean
public class ClientTopologyService implements TopologyService {

    private final OpsTopologyRestClient restClient;

    @Inject
    public ClientTopologyService(@RestClient OpsTopologyRestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public TopologySnapshot getSnapshot(String tenancyId) {
        var                response = restClient.getTopology(tenancyId);
        List<TopologyNode> nodes    = new ArrayList<>();
        for (var loop : response.loops()) {
            Map<String, String> actualMap = loop.actualStatuses().stream()
                                                .collect(Collectors.toMap(
                                                        OpsNodeStatusDto::nodeId, OpsNodeStatusDto::status));
            for (var desired : loop.desiredNodes()) {
                String actualStatus = actualMap.getOrDefault(desired.nodeId(), "UNKNOWN");
                nodes.add(new TopologyNode(
                        desired.nodeId(), desired.nodeType(),
                        desired.targetStatus(), actualStatus,
                        loop.loopKey(), desired.adapted()));
            }
        }
        return new TopologySnapshot(nodes);
    }
}
