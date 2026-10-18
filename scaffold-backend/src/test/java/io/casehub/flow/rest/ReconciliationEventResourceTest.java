package io.casehub.flow.rest;

import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReconciliationEventResourceTest {

    @Test
    void resourceAnnotatedCorrectly() throws Exception {
        var pathAnnotation = ReconciliationEventResource.class.getAnnotation(jakarta.ws.rs.Path.class);
        assertThat(pathAnnotation).isNotNull();
        assertThat(pathAnnotation.value()).isEqualTo("/api/ops/reconciliation/events");

        var getMethod = ReconciliationEventResource.class.getMethod("streamEvents");
        var produces = getMethod.getAnnotation(jakarta.ws.rs.Produces.class);
        assertThat(produces.value()).contains(MediaType.SERVER_SENT_EVENTS);
    }
}
