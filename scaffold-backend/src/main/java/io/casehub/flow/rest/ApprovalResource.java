package io.casehub.flow.rest;

import io.casehub.flow.service.ApprovalDetail;
import io.casehub.flow.service.ApprovalService;
import io.casehub.flow.service.ApprovalSummary;
import io.casehub.platform.api.identity.CurrentPrincipal;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import java.util.List;
import java.util.UUID;

@Path("/api/scaffold/approvals")
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
@RunOnVirtualThread
public class ApprovalResource {

    @Inject ApprovalService approvalService;
    @Inject CurrentPrincipal currentPrincipal;

    @GET
    public List<ApprovalSummary> listPendingApprovals() {
        return approvalService.listPendingApprovals(currentPrincipal.tenancyId());
    }

    @GET
    @Path("/{approvalId}")
    public ApprovalDetail getApproval(@PathParam("approvalId") UUID approvalId) {
        return approvalService.getApproval(approvalId);
    }

    @POST
    @Path("/{approvalId}/approve")
    public void approve(@PathParam("approvalId") UUID approvalId) {
        approvalService.approve(approvalId, currentPrincipal.actorId());
    }

    @POST
    @Path("/{approvalId}/reject")
    public void reject(@PathParam("approvalId") UUID approvalId, @QueryParam("reason") String reason) {
        approvalService.reject(approvalId, currentPrincipal.actorId(), reason);
    }
}
