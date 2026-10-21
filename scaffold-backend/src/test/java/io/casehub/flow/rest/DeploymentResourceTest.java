package io.casehub.flow.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DeploymentResourceTest {

    @Test
    void resourceAnnotatedWithCorrectPath() {
        var path = DeploymentResource.class.getAnnotation(Path.class);
        assertThat(path).isNotNull();
        assertThat(path.value()).isEqualTo("/api/scaffold/deployments");
    }

    @Test
    void listDeploymentsEndpointExists() throws Exception {
        var method = DeploymentResource.class.getMethod("listDeployments", UUID.class);
        assertThat(method.getAnnotation(GET.class)).isNotNull();
    }

    @Test
    void deployEndpointExists() throws Exception {
        var method = DeploymentResource.class.getMethod("deploy", UUID.class);
        assertThat(method.getAnnotation(POST.class)).isNotNull();
    }
}
