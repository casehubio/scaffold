package io.casehub.flow.rest;

import io.casehub.flow.service.ReconciliationService;
import io.casehub.flow.service.ReconciliationStatus;
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

import java.util.UUID;

@Path("/api/scaffold/reconciliation")
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
@RunOnVirtualThread
public class ReconciliationResource {

    @Inject
    ReconciliationService reconciliationService;

    @GET
    @Path("/{applicationId}/status")
    public ReconciliationStatus getStatus(@RestPath UUID applicationId) {
        return reconciliationService.getStatus(applicationId);
    }

    @POST
    @Path("/{applicationId}/trigger")
    public Response triggerReconciliation(@RestPath UUID applicationId) {
        reconciliationService.triggerReconciliation(applicationId);
        return Response.noContent().build();
    }
}
