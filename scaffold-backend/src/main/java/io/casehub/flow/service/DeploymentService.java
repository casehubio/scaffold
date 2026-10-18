package io.casehub.flow.service;

import java.util.List;

public interface DeploymentService {
    List<TopologyDefinition> availableDefinitions();
    List<DeploymentTarget> availableTargets();
    DeploymentStatus deploy(String definitionId, String targetId);
    DeploymentStatus status(String deploymentId);
}
