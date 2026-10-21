package io.casehub.flow.rest;

import io.casehub.flow.service.DeploymentRecord;
import io.casehub.flow.service.DeploymentService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import io.smallrye.common.annotation.RunOnVirtualThread;
import org.jboss.resteasy.reactive.RestPath;

import java.util.List;
import java.util.UUID;

@Path("/api/scaffold/deployments")
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
@RunOnVirtualThread
public class DeploymentResource {

    @Inject
    DeploymentService deploymentService;

    @GET
    @Path("/{applicationId}")
    public List<DeploymentRecord> listDeployments(@RestPath UUID applicationId) {
        return deploymentService.listDeployments(applicationId);
    }

    @GET
    @Path("/{applicationId}/current")
    public Response getCurrentDeployment(@RestPath UUID applicationId) {
        var record = deploymentService.getCurrentDeployment(applicationId);
        if (record == null) {return Response.status(404).build();}
        return Response.ok(record).build();
    }

    @POST
    @Path("/{applicationId}/deploy")
    public Response deploy(@RestPath UUID applicationId) {
        deploymentService.deploy(applicationId);
        return Response.noContent().build();
    }

    @POST
    @Path("/{applicationId}/rollback")
    public Response rollback(@RestPath UUID applicationId) {
        deploymentService.rollback(applicationId);
        return Response.noContent().build();
    }
}
