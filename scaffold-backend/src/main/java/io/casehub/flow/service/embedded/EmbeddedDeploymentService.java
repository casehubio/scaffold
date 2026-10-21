package io.casehub.flow.service.embedded;

import io.casehub.flow.service.DeploymentRecord;
import io.casehub.flow.service.DeploymentService;
import io.casehub.ops.service.api.OpsDeploymentApi;
import io.casehub.ops.service.entity.DeploymentRecordEntity;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
@Alternative
@Priority(100)
public class EmbeddedDeploymentService implements DeploymentService {

    @Inject
    OpsDeploymentApi deploymentApi;

    @Override
    public List<DeploymentRecord> listDeployments(UUID applicationId) {
        return deploymentApi.listDeployments(applicationId).stream()
                            .map(EmbeddedDeploymentService::toRecord)
                            .toList();
    }

    @Override
    public DeploymentRecord getCurrentDeployment(UUID applicationId) {
        var entity = deploymentApi.getCurrentDeployment(applicationId);
        return entity != null ? toRecord(entity) : null;
    }

    @Override
    public void deploy(UUID applicationId) {
        deploymentApi.deploy(applicationId);
    }

    @Override
    public void rollback(UUID applicationId) {
        deploymentApi.rollback(applicationId);
    }

    static DeploymentRecord toRecord(DeploymentRecordEntity entity) {
        return new DeploymentRecord(
                entity.id.toString(),
                entity.outcome != null ? entity.outcome.name() : "UNKNOWN",
                "",
                entity.createdAt);
    }
}
