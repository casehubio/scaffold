package io.casehub.flow.service;

import java.util.List;
import java.util.UUID;

public interface DeploymentService {
    List<DeploymentRecord> listDeployments(UUID applicationId);

    DeploymentRecord getCurrentDeployment(UUID applicationId);

    void deploy(UUID applicationId);

    void rollback(UUID applicationId);
}
