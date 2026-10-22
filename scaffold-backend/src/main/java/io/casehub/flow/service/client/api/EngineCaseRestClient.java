package io.casehub.flow.service.client.api;

import io.casehub.api.model.CaseStatus;
import io.casehub.api.view.CaseInstanceView;
import io.casehub.api.view.CasePage;
import io.casehub.api.view.PlanItemView;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.List;
import java.util.UUID;

@RegisterRestClient(configKey = "ops-api")
@Path("/api/engine/cases")
@Produces(MediaType.APPLICATION_JSON)
public interface EngineCaseRestClient {

    @GET
    CasePage listCases(@QueryParam("status") CaseStatus status,
                       @QueryParam("namespace") String namespace,
                       @QueryParam("name") String name,
                       @QueryParam("tenancyId") String tenancyId,
                       @QueryParam("offset") Integer offset,
                       @QueryParam("limit") Integer limit);

    @GET
    @Path("/{caseId}")
    CaseInstanceView getCaseById(@PathParam("caseId") UUID caseId,
                                  @QueryParam("tenancyId") String tenancyId);

    @GET
    @Path("/{caseId}/plan-items")
    List<PlanItemView> getPlanItems(@PathParam("caseId") UUID caseId,
                                    @QueryParam("tenancyId") String tenancyId);
}
