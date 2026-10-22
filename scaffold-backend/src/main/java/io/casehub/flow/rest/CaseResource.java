package io.casehub.flow.rest;

import io.casehub.flow.service.CaseDetail;
import io.casehub.flow.service.CaseService;
import io.casehub.flow.service.CaseSummary;
import io.casehub.platform.api.identity.CurrentPrincipal;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;
import java.util.UUID;

@Path("/api/scaffold/cases")
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
@RunOnVirtualThread
public class CaseResource {

    @Inject CaseService caseService;
    @Inject CurrentPrincipal currentPrincipal;

    @GET
    public List<CaseSummary> listCases() {
        return caseService.listActiveCases(currentPrincipal.tenancyId());
    }

    @GET
    @Path("/{caseId}")
    public CaseDetail getCaseDetail(@PathParam("caseId") UUID caseId) {
        return caseService.getCaseDetail(caseId, currentPrincipal.tenancyId());
    }
}
