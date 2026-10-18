package io.casehub.flow.rest;

import io.cloudevents.CloudEvent;
import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.ObservesAsync;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.RestStreamElementType;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

@Path("/api/ops/reconciliation/events")
@ApplicationScoped
public class ReconciliationEventResource {

    private final CopyOnWriteArrayList<Consumer<String>> subscribers = new CopyOnWriteArrayList<>();

    @GET
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @RestStreamElementType(MediaType.APPLICATION_JSON)
    public Multi<String> streamEvents() {
        return Multi.createFrom().emitter(emitter -> {
            Consumer<String> subscriber = emitter::emit;
            subscribers.add(subscriber);
            emitter.onTermination(() -> subscribers.remove(subscriber));
        });
    }

    void onCloudEvent(@ObservesAsync CloudEvent event) {
        if (event.getType() != null && event.getType().startsWith("io.casehub.desiredstate.")) {
            var json = serializeEvent(event);
            for (var subscriber : subscribers) {
                subscriber.accept(json);
            }
        }
    }

    private String serializeEvent(CloudEvent event) {
        return String.format("{\"type\":\"%s\",\"source\":\"%s\",\"subject\":\"%s\",\"time\":\"%s\"}",
            event.getType(), event.getSource(), event.getSubject(),
            event.getTime() != null ? event.getTime().toString() : "");
    }
}
