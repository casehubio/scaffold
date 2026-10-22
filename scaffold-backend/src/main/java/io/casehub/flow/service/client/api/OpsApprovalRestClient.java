package io.casehub.flow.service.client.api;

import io.casehub.ops.service.rest.ApprovalView;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.List;
import java.util.UUID;

@RegisterRestClient(configKey = "ops-api")
@Path("/api/ops/approvals")
@Produces(MediaType.APPLICATION_JSON)
public interface OpsApprovalRestClient {

    @GET
    List<ApprovalView> listApprovals();

    @GET
    @Path("/{id}")
    ApprovalView getApproval(@PathParam("id") UUID id);

    @POST
    @Path("/{id}/approve")
    @Consumes(MediaType.APPLICATION_JSON)
    void approve(@PathParam("id") UUID id, @QueryParam("actorId") String actorId);

    @POST
    @Path("/{id}/reject")
    @Consumes(MediaType.APPLICATION_JSON)
    void reject(@PathParam("id") UUID id, @QueryParam("actorId") String actorId, @QueryParam("reason") String reason);
}
