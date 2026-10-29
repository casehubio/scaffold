package io.casehub.flow.service.embedded;

import io.casehub.api.engine.rest.EngineCaseApi;
import io.casehub.api.view.StartCaseRequest;
import io.casehub.desiredstate.api.ActualState;
import io.casehub.desiredstate.api.DesiredStateGraph;
import io.casehub.desiredstate.api.NodeId;
import io.casehub.desiredstate.api.OrderedStep;
import io.casehub.desiredstate.api.TransitionPlan;
import io.casehub.desiredstate.runtime.TransitionPlanner;
import io.casehub.flow.service.DeployResult;
import io.casehub.flow.service.GraphEdge;
import io.casehub.flow.service.GraphNode;
import io.casehub.flow.service.GraphView;
import io.casehub.flow.service.PlanStep;
import io.casehub.flow.service.PlanView;
import io.casehub.flow.service.TopologyCatalogueService;
import io.casehub.flow.service.TopologyEntry;
import io.casehub.flow.service.TopologyPlan;
import io.casehub.ops.service.topology.TopologyCompilerService;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
@Alternative
@Priority(100)
public class EmbeddedTopologyCatalogueService implements TopologyCatalogueService {

    private final TopologyCompilerService compilerService;
    private final EngineCaseApi engineCaseApi;
    private final String topologyBasePath;
    private final TransitionPlanner planner = new TransitionPlanner();

    @Inject
    public EmbeddedTopologyCatalogueService(
            TopologyCompilerService compilerService,
            EngineCaseApi engineCaseApi,
            @ConfigProperty(name = "casehub.ops.topology.path") java.util.Optional<String> topologyBasePath) {
        this.compilerService  = compilerService;
        this.engineCaseApi    = engineCaseApi;
        this.topologyBasePath = topologyBasePath.orElse("");
    }

    @Override
    public List<TopologyEntry> listTopologies() {
        var descriptors = compilerService.listTopologies();
        List<TopologyEntry> entries = new ArrayList<>();
        for (var desc : descriptors) {
            var metadata = readMetadata(desc.name());
            entries.add(new TopologyEntry(desc.name(),
                    metadata.getOrDefault("description", ""),
                    metadata.getOrDefault("methodologyStep", ""),
                    metadata.getOrDefault("prerequisites", "")));
        }
        return entries;
    }

    @Override
    public TopologyPlan getTopologyPlan(String name) {
        DesiredStateGraph graph = compilerService.compileTopology(name);
        var transitionPlan = planner.plan(graph, new ActualState(Map.of()));

        GraphView graphView = toGraphView(graph);
        PlanView planView = toPlanView(transitionPlan);
        String playbookYaml = readFileFromTopology(name, "playbook.yaml");
        String readme = readFileFromTopology(name, "README.md");

        return new TopologyPlan(graphView, planView, playbookYaml, readme);
    }

    @Override
    public DeployResult deploy(String name, String tenancyId) {
        if (compilerService.isRunning(tenancyId, name)) {
            throw new IllegalStateException("Topology already deployed: " + name);
        }

        String loopKey = compilerService.deploy(tenancyId, topologyBasePath, name);

        try {
            var caseView = engineCaseApi.startCase(
                    new StartCaseRequest("scaffold", "application-lifecycle", "1.0.0",
                            Map.of("topologyName", name, "applicationId", name)),
                    tenancyId);
            return new DeployResult(caseView.caseId(), loopKey);
        } catch (Exception e) {
            compilerService.stop(tenancyId, name);
            throw new IllegalStateException("Case creation failed after deploy — reconciliation stopped", e);
        }
    }

    private GraphView toGraphView(DesiredStateGraph graph) {
        List<GraphNode> nodes = graph.nodes().entrySet().stream()
                .map(e -> new GraphNode(e.getKey().value(),
                        e.getValue().type().value(),
                        graph.dependenciesOf(e.getKey()).stream()
                                .map(NodeId::value).toList()))
                .toList();
        List<GraphEdge> edges = graph.dependencies().stream()
                .map(d -> new GraphEdge(d.from().value(), d.to().value()))
                .toList();
        return new GraphView(nodes, edges);
    }

    private PlanView toPlanView(TransitionPlan plan) {
        return new PlanView(
                toLayers(plan.additions()),
                toLayers(plan.removals()),
                toLayers(plan.suspensions()),
                toLayers(plan.resumptions()),
                plan.isEmpty());
    }

    private List<List<PlanStep>> toLayers(List<List<OrderedStep>> layers) {
        return layers.stream()
                .map(layer -> layer.stream()
                        .map(step -> new PlanStep(step.node().id().value(),
                                step.action().name(), step.node().type().value()))
                        .toList())
                .toList();
    }

    private Map<String, String> readMetadata(String name) {
        String content = readFileFromTopology(name, "topology-metadata.yaml");
        if (content == null) return Map.of();
        Map<String, String> result = new LinkedHashMap<>();
        for (String line : content.split("\n")) {
            int colon = line.indexOf(':');
            if (colon > 0) {
                String key = line.substring(0, colon).trim();
                String value = line.substring(colon + 1).trim().replaceAll("^\"|\"$", "");
                result.put(key, value);
            }
        }
        return result;
    }

    private String readFileFromTopology(String topologyName, String fileName) {
        Path file = Path.of(topologyBasePath, topologyName, fileName);
        if (!Files.isRegularFile(file)) return null;
        try {
            return Files.readString(file);
        } catch (IOException e) {
            return null;
        }
    }
}
