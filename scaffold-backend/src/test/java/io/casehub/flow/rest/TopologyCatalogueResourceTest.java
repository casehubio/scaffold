package io.casehub.flow.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TopologyCatalogueResourceTest {

    @Test
    void resourceAnnotatedWithCorrectPath() {
        var path = TopologyCatalogueResource.class.getAnnotation(Path.class);
        assertThat(path).isNotNull();
        assertThat(path.value()).isEqualTo("/api/scaffold/catalogue");
    }

    @Test
    void listEndpointExists() throws Exception {
        var method = TopologyCatalogueResource.class.getMethod("listTopologies");
        assertThat(method.getAnnotation(GET.class)).isNotNull();
    }

    @Test
    void planEndpointExists() throws Exception {
        var method = TopologyCatalogueResource.class.getMethod("getTopologyPlan", String.class);
        assertThat(method.getAnnotation(GET.class)).isNotNull();
    }

    @Test
    void deployEndpointExists() throws Exception {
        var method = TopologyCatalogueResource.class.getMethod("deploy", String.class);
        assertThat(method.getAnnotation(POST.class)).isNotNull();
    }
}
