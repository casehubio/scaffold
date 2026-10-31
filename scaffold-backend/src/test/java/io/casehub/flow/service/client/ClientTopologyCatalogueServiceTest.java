package io.casehub.flow.service.client;

import io.casehub.flow.service.DeployResult;
import io.casehub.flow.service.TopologyEntry;
import io.casehub.flow.service.TopologyPlan;
import io.casehub.flow.service.client.api.ScaffoldCatalogueRestClient;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ClientTopologyCatalogueServiceTest {

    private final ScaffoldCatalogueRestClient restClient = mock(ScaffoldCatalogueRestClient.class);
    private final ClientTopologyCatalogueService service = new ClientTopologyCatalogueService(restClient);

    @Test
    void listTopologiesDelegates() {
        var entries = List.of(new TopologyEntry("01-hello-llm", "desc", "1", "none"));
        when(restClient.listTopologies()).thenReturn(entries);

        assertThat(service.listTopologies()).isEqualTo(entries);
    }

    @Test
    void getTopologyPlanDelegates() {
        var plan = mock(TopologyPlan.class);
        when(restClient.getTopologyPlan("01-hello-llm")).thenReturn(plan);

        assertThat(service.getTopologyPlan("01-hello-llm")).isEqualTo(plan);
    }

    @Test
    void deployDelegates() {
        var caseId = UUID.randomUUID();
        var result = new DeployResult(caseId, "loop-1");
        when(restClient.deploy("01-hello-llm")).thenReturn(result);

        var actual = service.deploy("01-hello-llm", "tenant-1");
        assertThat(actual.caseId()).isEqualTo(caseId);
        assertThat(actual.loopKey()).isEqualTo("loop-1");
    }
}
