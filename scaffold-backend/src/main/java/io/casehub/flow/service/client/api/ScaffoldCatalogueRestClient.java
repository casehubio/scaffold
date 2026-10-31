package io.casehub.flow.service.client.api;

import io.casehub.flow.service.DeployResult;
import io.casehub.flow.service.TopologyEntry;
import io.casehub.flow.service.TopologyPlan;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.List;

@RegisterRestClient(configKey = "catalogue-api")
@Path("/api/scaffold/catalogue")
@Produces(MediaType.APPLICATION_JSON)
public interface ScaffoldCatalogueRestClient {

    @GET
    List<TopologyEntry> listTopologies();

    @GET
    @Path("/{name}/plan")
    TopologyPlan getTopologyPlan(@PathParam("name") String name);

    @POST
    @Path("/{name}/deploy")
    DeployResult deploy(@PathParam("name") String name);
}
