package io.casehub.flow.rest;

import io.casehub.flow.service.DeployResult;
import io.casehub.flow.service.TopologyCatalogueService;
import io.casehub.flow.service.TopologyEntry;
import io.casehub.flow.service.TopologyPlan;
import io.casehub.platform.api.identity.CurrentPrincipal;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Map;

@Path("/api/scaffold/catalogue")
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
@RunOnVirtualThread
public class TopologyCatalogueResource {

    @Inject TopologyCatalogueService catalogueService;
    @Inject CurrentPrincipal currentPrincipal;

    @GET
    public List<TopologyEntry> listTopologies() {
        return catalogueService.listTopologies();
    }

    @GET
    @Path("/{name}/plan")
    public TopologyPlan getTopologyPlan(@PathParam("name") String name) {
        return catalogueService.getTopologyPlan(name);
    }

    @POST
    @Path("/{name}/deploy")
    public Response deploy(@PathParam("name") String name) {
        try {
            DeployResult result = catalogueService.deploy(name, currentPrincipal.tenancyId());
            return Response.status(201).entity(result).build();
        } catch (IllegalStateException e) {
            if (e.getMessage() != null && e.getMessage().contains("already deployed")) {
                return Response.status(409).entity(Map.of("error", e.getMessage())).build();
            }
            throw e;
        }
    }
}
