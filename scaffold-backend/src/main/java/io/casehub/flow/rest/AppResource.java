package io.casehub.flow.rest;

import io.casehub.flow.app.AppProducer;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/app")
@Produces(MediaType.APPLICATION_JSON)
public class AppResource {

    private final AppProducer appProducer;

    @Inject
    public AppResource(AppProducer appProducer) {
        this.appProducer = appProducer;
    }

    @GET
    public Response getApp() {
        return appProducer.getConfig()
                          .map(c -> Response.ok(c).build())
                          .orElse(Response.status(404).build());
    }
}
