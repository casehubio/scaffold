package io.casehub.flow.rest;

import io.casehub.flow.service.TopologyService;
import io.casehub.flow.service.TopologySnapshot;
import io.casehub.platform.api.identity.CurrentPrincipal;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/api/scaffold/topology")
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class TopologyResource {

    @Inject
    TopologyService  topologyService;
    @Inject
    CurrentPrincipal currentPrincipal;

    @GET
    public TopologySnapshot getSnapshot() {
        return topologyService.getSnapshot(currentPrincipal.tenancyId());
    }
}
