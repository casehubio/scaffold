package io.casehub.flow.service.client.api;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "ops-api")
@Path("/api/ops/topology")
@Produces(MediaType.APPLICATION_JSON)
public interface OpsTopologyRestClient {

    @GET
    OpsTopologyResponse getTopology(@QueryParam("tenancyId") String tenancyId);
}
