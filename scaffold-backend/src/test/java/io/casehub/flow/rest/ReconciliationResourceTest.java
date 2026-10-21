package io.casehub.flow.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ReconciliationResourceTest {

    @Test
    void resourceAnnotatedWithCorrectPath() {
        var path = ReconciliationResource.class.getAnnotation(Path.class);
        assertThat(path).isNotNull();
        assertThat(path.value()).isEqualTo("/api/scaffold/reconciliation");
    }

    @Test
    void getStatusEndpointExists() throws Exception {
        var method = ReconciliationResource.class.getMethod("getStatus", UUID.class);
        assertThat(method.getAnnotation(GET.class)).isNotNull();
    }

    @Test
    void triggerEndpointExists() throws Exception {
        var method = ReconciliationResource.class.getMethod("triggerReconciliation", UUID.class);
        assertThat(method.getAnnotation(POST.class)).isNotNull();
    }
}
