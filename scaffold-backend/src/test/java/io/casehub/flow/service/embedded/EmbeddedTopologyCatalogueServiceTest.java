package io.casehub.flow.service.embedded;

import io.casehub.api.engine.rest.EngineCaseApi;
import io.casehub.desiredstate.api.Dependency;
import io.casehub.desiredstate.api.DesiredNode;
import io.casehub.desiredstate.api.HumanGating;
import io.casehub.desiredstate.api.NodeId;
import io.casehub.desiredstate.runtime.DefaultDesiredStateGraphFactory;
import io.casehub.ops.service.topology.TopologyCompilerService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EmbeddedTopologyCatalogueServiceTest {

    @Test
    void getTopologyPlanReturnsGraphAndLayeredPlan() {
        var compilerService = mock(TopologyCompilerService.class);
        var engineCaseApi   = mock(EngineCaseApi.class);

        var factory = new DefaultDesiredStateGraphFactory();
        var spec1   = mock(io.casehub.desiredstate.api.NodeSpec.class);
        when(spec1.nodeType()).thenReturn(io.casehub.desiredstate.api.NodeType.of("casehub_application"));
        var spec2 = mock(io.casehub.desiredstate.api.NodeSpec.class);
        when(spec2.nodeType()).thenReturn(io.casehub.desiredstate.api.NodeType.of("casehub_application"));

        var node1 = new DesiredNode(NodeId.of("manifest"), spec1, HumanGating.NONE);
        var node2 = new DesiredNode(NodeId.of("eidos"), spec2, HumanGating.NONE);
        var graph = factory.of(List.of(node1, node2),
                               List.of(new Dependency(NodeId.of("eidos"), NodeId.of("manifest"))));

        when(compilerService.compileTopology("01-hello-llm")).thenReturn(graph);

        var service = new EmbeddedTopologyCatalogueService(
                compilerService, engineCaseApi, java.util.Optional.of("/test/topologies/deployment"));
        var plan = service.getTopologyPlan("01-hello-llm");

        assertThat(plan.graph().nodes()).hasSize(2);
        assertThat(plan.plan().additions()).isNotEmpty();
        assertThat(plan.plan().empty()).isFalse();
    }
}
