package io.casehub.flow.service.embedded;

import io.casehub.flow.service.DeploymentRecord;
import io.casehub.flow.service.DeploymentService;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
@Alternative
@Priority(100)
public class EmbeddedDeploymentService implements DeploymentService {
    @Override
    public List<DeploymentRecord> listDeployments(UUID applicationId) {
        return List.of();
    }

    @Override
    public DeploymentRecord getCurrentDeployment(UUID applicationId) {
        return null;
    }

    @Override
    public void deploy(UUID applicationId) {}

    @Override
    public void rollback(UUID applicationId) {}
}
