package io.casehub.flow.service.client;

import io.casehub.flow.service.DeploymentRecord;
import io.casehub.flow.service.DeploymentService;
import io.casehub.flow.service.client.api.OpsDeploymentRecordDto;
import io.casehub.flow.service.client.api.OpsDeploymentRestClient;
import io.quarkus.arc.DefaultBean;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
@DefaultBean
public class ClientDeploymentService implements DeploymentService {

    @Inject
    @RestClient
    OpsDeploymentRestClient restClient;

    @Override
    public List<DeploymentRecord> listDeployments(UUID applicationId) {
        return restClient.listDeployments(applicationId).stream()
                         .map(ClientDeploymentService::toRecord)
                         .toList();
    }

    @Override
    public DeploymentRecord getCurrentDeployment(UUID applicationId) {
        var dto = restClient.getCurrentDeployment(applicationId);
        return dto != null ? toRecord(dto) : null;
    }

    @Override
    public void deploy(UUID applicationId) {
        restClient.deploy(applicationId);
    }

    @Override
    public void rollback(UUID applicationId) {
        restClient.rollback(applicationId);
    }

    static DeploymentRecord toRecord(OpsDeploymentRecordDto dto) {
        return new DeploymentRecord(dto.id(), dto.outcome(), "", dto.createdAt());
    }
}
