package io.casehub.flow.service.client;

import io.casehub.flow.service.DeploymentService;
import io.casehub.flow.service.DeploymentStatus;
import io.casehub.flow.service.DeploymentTarget;
import io.casehub.flow.service.TopologyDefinition;
import io.quarkus.arc.DefaultBean;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
@DefaultBean
public class ClientDeploymentService implements DeploymentService {

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
