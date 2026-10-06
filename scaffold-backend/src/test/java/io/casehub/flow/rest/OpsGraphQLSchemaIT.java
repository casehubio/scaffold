package io.casehub.flow.rest;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpsGraphQLSchemaIT {

    @Test
    void opsServiceApiClassesAreOnClasspath() throws ClassNotFoundException {
        Class.forName("io.casehub.ops.service.api.OpsApplicationApi");
        Class.forName("io.casehub.ops.service.api.OpsDeploymentApi");
        Class.forName("io.casehub.ops.service.api.OpsClusterApi");
        Class.forName("io.casehub.ops.service.api.OpsReconciliationApi");
        Class.forName("io.casehub.ops.service.api.OpsCaseApi");
        Class.forName("io.casehub.ops.service.api.OpsApprovalApi");
    }

    @Test
    void opsServiceApiClassesHaveMcpDomainAnnotation() throws ClassNotFoundException {
        var cls = Class.forName("io.casehub.ops.service.api.OpsApplicationApi");
        assertThat(cls.getAnnotations())
            .anyMatch(a -> a.annotationType().getSimpleName().equals("McpDomain"));
    }
}
