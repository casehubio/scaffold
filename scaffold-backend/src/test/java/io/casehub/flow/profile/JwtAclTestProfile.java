package io.casehub.flow.profile;

import io.quarkus.test.junit.QuarkusTestProfile;
import java.util.Map;

/**
 * Test profile that activates JPA-backed ACL + JWT-aware CurrentPrincipal.
 *
 * <p>JwtAwareTestPrincipal reads actorId/tenancyId/groups from SecurityIdentity and JWT
 * claims populated by @TestSecurity + @OidcSecurity annotations. Falls back to config
 * properties during startup.
 */
public class JwtAclTestProfile implements QuarkusTestProfile {

  @Override
  public Map<String, String> getConfigOverrides() {
      return Map.ofEntries(
              Map.entry("quarkus.arc.exclude-types",
                  "io.casehub.platform.oidc.SecurityIdentityCurrentPrincipal,io.casehub.work.runtime.service.NoOpGroupMembershipProvider,io.casehub.work.runtime.service.TenantScopedPrincipal,io.casehub.connectors.**,io.casehub.engine.rest.health.AclEnforcementHealthCheck"),
              Map.entry("quarkus.oidc.devservices.enabled", "false"),
              Map.entry("casehub.platform.principal.actorId", "test-actor"),
              Map.entry("casehub.tenancy.default-id", "test-tenant"),
              Map.entry("casehub.cbr.retention.case-types", "test"),
              Map.entry("casehub.cbr.retention.domain", "test"),
              Map.entry("casehub.cbr.trust-retention.case-types", "test"),
              Map.entry("casehub.cbr.trust-retention.domain", "test"),
              Map.entry("casehub.memory.retention.domain", "test"),
              Map.entry("casehub.ops.approval.roles.critical", "ops-admin"),
              Map.entry("casehub.ops.approval.roles.high", "ops-approver"),
              Map.entry("casehub.ops.k8s.approval.critical-namespaces", "kube-system"),
              Map.entry("casehub.ops.k8s.approval.production-namespaces", "production"));
  }
}
