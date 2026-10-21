package io.casehub.flow.service.client.api;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.reactive.RestPath;

import java.util.List;
import java.util.UUID;

@RegisterRestClient(configKey = "ops-api")
@Path("/api/ops/deployments")
@Produces(MediaType.APPLICATION_JSON)
public interface OpsDeploymentRestClient {

    @GET
    @Path("/{applicationId}")
    List<OpsDeploymentRecordDto> listDeployments(@RestPath UUID applicationId);

    @GET
    @Path("/{applicationId}/current")
    OpsDeploymentRecordDto getCurrentDeployment(@RestPath UUID applicationId);

    @POST
    @Path("/{applicationId}/deploy")
    void deploy(@RestPath UUID applicationId);

    @POST
    @Path("/{applicationId}/rollback")
    void rollback(@RestPath UUID applicationId);
}
