package io.casehub.flow.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TopologyResourceTest {

    @Test
    void resourceAnnotatedWithCorrectPath() {
        var path = TopologyResource.class.getAnnotation(Path.class);
        assertThat(path).isNotNull();
        assertThat(path.value()).isEqualTo("/api/scaffold/topology");
    }

    @Test
    void getSnapshotEndpointExists() throws Exception {
        var method = TopologyResource.class.getMethod("getSnapshot");
        assertThat(method.getAnnotation(GET.class)).isNotNull();
        var produces = TopologyResource.class.getAnnotation(Produces.class);
        assertThat(produces.value()).contains(MediaType.APPLICATION_JSON);
    }
}
