package io.casehub.flow.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApprovalResourceTest {

    @Test
    void resourceAnnotatedWithCorrectPath() {
        var path = ApprovalResource.class.getAnnotation(Path.class);
        assertThat(path).isNotNull();
        assertThat(path.value()).isEqualTo("/api/scaffold/approvals");
    }

    @Test
    void listEndpointExists() throws Exception {
        var method = ApprovalResource.class.getMethod("listPendingApprovals");
        assertThat(method.getAnnotation(GET.class)).isNotNull();
    }

    @Test
    void approveEndpointExists() throws Exception {
        var method = ApprovalResource.class.getMethod("approve", java.util.UUID.class);
        assertThat(method.getAnnotation(POST.class)).isNotNull();
    }

    @Test
    void rejectEndpointExists() throws Exception {
        var method = ApprovalResource.class.getMethod("reject", java.util.UUID.class, String.class);
        assertThat(method.getAnnotation(POST.class)).isNotNull();
    }
}
