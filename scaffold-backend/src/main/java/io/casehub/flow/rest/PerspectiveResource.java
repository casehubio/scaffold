package io.casehub.flow.rest;

import io.casehub.flow.perspective.PerspectiveProducer;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/perspective")
@Produces(MediaType.APPLICATION_JSON)
public class PerspectiveResource {

    private final PerspectiveProducer perspectiveProducer;

    @Inject
    public PerspectiveResource(PerspectiveProducer perspectiveProducer) {
        this.perspectiveProducer = perspectiveProducer;
    }

    @GET
    public Response getPerspective() {
        return perspectiveProducer.getConfig()
            .map(c -> Response.ok(c).build())
            .orElse(Response.status(404).build());
    }
}
