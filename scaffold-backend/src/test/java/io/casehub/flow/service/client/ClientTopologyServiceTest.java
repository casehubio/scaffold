package io.casehub.flow.service.client;

import io.casehub.flow.service.client.api.OpsLoopTopologyDto;
import io.casehub.flow.service.client.api.OpsNodeStatusDto;
import io.casehub.flow.service.client.api.OpsTopologyNodeDto;
import io.casehub.flow.service.client.api.OpsTopologyResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ClientTopologyServiceTest {

    @Test
    void flattensRestResponseToNodeList() {
        var response = new OpsTopologyResponse(List.of(
                new OpsLoopTopologyDto("loop-1",
                        List.of(new OpsTopologyNodeDto("n-1", "container", "RUNNING", false)),
                        List.of(new OpsNodeStatusDto("n-1", "RUNNING")))
        ));

        var service = new ClientTopologyService(tenancyId -> response);
        var snapshot = service.getSnapshot("tenant");
        assertThat(snapshot.nodes()).hasSize(1);
        assertThat(snapshot.nodes().get(0).nodeId()).isEqualTo("n-1");
        assertThat(snapshot.nodes().get(0).actualStatus()).isEqualTo("RUNNING");
    }

    @Test
    void missingActualStatusDefaultsToUnknown() {
        var response = new OpsTopologyResponse(List.of(
                new OpsLoopTopologyDto("loop-1",
                        List.of(new OpsTopologyNodeDto("n-1", "container", "RUNNING", false)),
                        List.of())
        ));

        var service = new ClientTopologyService(tenancyId -> response);
        var snapshot = service.getSnapshot("tenant");
        assertThat(snapshot.nodes().get(0).actualStatus()).isEqualTo("UNKNOWN");
    }
}
