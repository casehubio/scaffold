package io.casehub.flow.service.client;

import io.casehub.flow.service.DeploymentRecord;
import io.casehub.flow.service.DeploymentService;
import io.quarkus.arc.DefaultBean;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
@DefaultBean
public class ClientDeploymentService implements DeploymentService {
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
