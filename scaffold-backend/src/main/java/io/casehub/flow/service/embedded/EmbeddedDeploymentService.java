package io.casehub.flow.service.embedded;

import io.casehub.flow.service.DeploymentService;
import io.casehub.flow.service.DeploymentStatus;
import io.casehub.flow.service.DeploymentTarget;
import io.casehub.flow.service.TopologyDefinition;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;

import java.util.List;

@ApplicationScoped
@Alternative
@Priority(100)
public class EmbeddedDeploymentService implements DeploymentService {

    @Override
    public List<TopologyDefinition> availableDefinitions() {
        return List.of();
    }

    @Override
    public List<DeploymentTarget> availableTargets() {
        return List.of();
    }

    @Override
    public DeploymentStatus deploy(String definitionId, String targetId) {
        return new DeploymentStatus("none", "PENDING", "");
    }

    @Override
    public DeploymentStatus status(String deploymentId) {
        return new DeploymentStatus(deploymentId, "UNKNOWN", "");
    }
}
