package io.casehub.flow.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CaseResourceTest {

    @Test
    void resourceAnnotatedWithCorrectPath() {
        var path = CaseResource.class.getAnnotation(Path.class);
        assertThat(path).isNotNull();
        assertThat(path.value()).isEqualTo("/api/scaffold/cases");
    }

    @Test
    void listEndpointExists() throws Exception {
        var method = CaseResource.class.getMethod("listCases");
        assertThat(method.getAnnotation(GET.class)).isNotNull();
    }

    @Test
    void detailEndpointExists() throws Exception {
        var method = CaseResource.class.getMethod("getCaseDetail", java.util.UUID.class);
        assertThat(method.getAnnotation(GET.class)).isNotNull();
    }
}
