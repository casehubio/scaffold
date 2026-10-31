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

    @Test
    void rejectsPathTraversal() {
        var compilerService = mock(TopologyCompilerService.class);
        var engineCaseApi   = mock(EngineCaseApi.class);

        var service = new EmbeddedTopologyCatalogueService(
                compilerService, engineCaseApi, java.util.Optional.of("/test/topologies/deployment"));

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.getTopologyPlan("../../etc"))
                                       .isInstanceOf(IllegalArgumentException.class)
                                       .hasMessageContaining("Invalid topology name");

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.deploy("../../etc", "tenant-1"))
                                       .isInstanceOf(IllegalArgumentException.class)
                                       .hasMessageContaining("Invalid topology name");
    }

    @Test
    void deployHappyPath() {
        var compilerService = mock(TopologyCompilerService.class);
        var engineCaseApi   = mock(EngineCaseApi.class);
        var caseView        = mock(io.casehub.api.view.CaseInstanceView.class);
        var caseId          = java.util.UUID.randomUUID();

        when(compilerService.isRunning("tenant-1", "01-hello-llm")).thenReturn(false);
        when(compilerService.deploy("tenant-1", "/test/topologies/deployment", "01-hello-llm")).thenReturn("loop-1");
        when(caseView.caseId()).thenReturn(caseId);
        when(engineCaseApi.startCase(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.eq("tenant-1"))).thenReturn(caseView);

        var service = new EmbeddedTopologyCatalogueService(
                compilerService, engineCaseApi, java.util.Optional.of("/test/topologies/deployment"));
        var result = service.deploy("01-hello-llm", "tenant-1");

        assertThat(result.caseId()).isEqualTo(caseId);
        assertThat(result.loopKey()).isEqualTo("loop-1");
    }

    @Test
    void deployCompensatesOnCaseCreationFailure() {
        var compilerService = mock(TopologyCompilerService.class);
        var engineCaseApi   = mock(EngineCaseApi.class);

        when(compilerService.isRunning("tenant-1", "01-hello-llm")).thenReturn(false);
        when(compilerService.deploy("tenant-1", "/test/topologies/deployment", "01-hello-llm")).thenReturn("loop-1");
        when(engineCaseApi.startCase(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.eq("tenant-1")))
                .thenThrow(new RuntimeException("engine unavailable"));

        var service = new EmbeddedTopologyCatalogueService(
                compilerService, engineCaseApi, java.util.Optional.of("/test/topologies/deployment"));

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.deploy("01-hello-llm", "tenant-1"))
                                       .isInstanceOf(IllegalStateException.class)
                                       .hasMessageContaining("Case creation failed");

        org.mockito.Mockito.verify(compilerService).stop("tenant-1", "01-hello-llm");
    }

    @Test
    void deployRejectsAlreadyDeployed() {
        var compilerService = mock(TopologyCompilerService.class);
        var engineCaseApi   = mock(EngineCaseApi.class);

        when(compilerService.isRunning("tenant-1", "01-hello-llm")).thenReturn(true);

        var service = new EmbeddedTopologyCatalogueService(
                compilerService, engineCaseApi, java.util.Optional.of("/test/topologies/deployment"));

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.deploy("01-hello-llm", "tenant-1"))
                                       .isInstanceOf(IllegalStateException.class)
                                       .hasMessageContaining("already deployed");
    }

    @Test
    void listTopologiesParsesMetadata() throws java.io.IOException {
        var compilerService = mock(TopologyCompilerService.class);
        var engineCaseApi   = mock(EngineCaseApi.class);

        java.nio.file.Path tempDir = java.nio.file.Files.createTempDirectory("topologies");
        java.nio.file.Path topoDir = java.nio.file.Files.createDirectory(tempDir.resolve("01-hello-llm"));
        java.nio.file.Files.writeString(topoDir.resolve("topology-metadata.yaml"),
                                        "description: Hello LLM topology\nmethodologyStep: \"1\"\nprerequisites: none\n");

        when(compilerService.listTopologies()).thenReturn(
                List.of(new io.casehub.ops.service.topology.TopologyDescriptor("01-hello-llm", topoDir.toString(), "local")));

        var service = new EmbeddedTopologyCatalogueService(
                compilerService, engineCaseApi, java.util.Optional.of(tempDir.toString()));
        var entries = service.listTopologies();

        assertThat(entries).hasSize(1);
        assertThat(entries.get(0).name()).isEqualTo("01-hello-llm");
        assertThat(entries.get(0).description()).isEqualTo("Hello LLM topology");
        assertThat(entries.get(0).methodologyStep()).isEqualTo("1");
        assertThat(entries.get(0).prerequisites()).isEqualTo("none");
    }


}
