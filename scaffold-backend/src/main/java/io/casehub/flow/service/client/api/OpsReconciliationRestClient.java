package io.casehub.flow.service.client.api;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.reactive.RestPath;

import java.util.UUID;

@RegisterRestClient(configKey = "ops-api")
@Path("/api/ops/reconciliation")
@Produces(MediaType.APPLICATION_JSON)
public interface OpsReconciliationRestClient {

    @GET
    @Path("/{applicationId}/status")
    OpsReconciliationStatusDto getStatus(@RestPath UUID applicationId);

    @POST
    @Path("/{applicationId}/trigger")
    void triggerReconciliation(@RestPath UUID applicationId);
}
