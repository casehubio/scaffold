package io.casehub.flow.rest;

import io.casehub.engine.rest.dto.ProblemDetail;
import org.jboss.logging.Logger;
import io.casehub.platform.api.acl.AccessControlProvider;
import io.casehub.platform.api.acl.AclAction;
import io.casehub.platform.api.acl.ResourceId;
import io.casehub.platform.api.identity.CurrentPrincipal;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

/**
 * ACL enforcement filter for engine-rest endpoints. Uses @PreMatching because
 * post-matching filters are not invoked in this RESTEasy Reactive deployment.
 * Offloads the blocking JPA canAccess() check via ManagedExecutor with CDI context propagation.
 * <p>
 * Replaced when engine#768 moves ACL into the engine-rest SPI. See scaffold#36.
 */
@Provider
@PreMatching
public class AclRequestFilter implements ContainerRequestFilter {

    private static final Logger LOG = Logger.getLogger(AclRequestFilter.class);

    @Override
    public void filter(ContainerRequestContext ctx) {
        String path = ctx.getUriInfo().getRequestUri().getPath();
        if (path.startsWith("/")) {path = path.substring(1);}

        if (!path.startsWith("api/v1/")) {return;}

        String   method   = ctx.getMethod();
        String[] segments = path.substring("api/v1/".length()).split("/");
        for (int i = 0; i < segments.length; i++) {
            segments[i] = java.net.URLDecoder.decode(segments[i], java.nio.charset.StandardCharsets.UTF_8);
        }

        ResourceId resourceId;
        AclAction  action;

        if (segments.length >= 3 && "case-definitions".equals(segments[0])) {
            resourceId = new ResourceId("CASE_DEFINITION", segments[1] + "/" + segments[2]);
            action     = AclAction.READ;
        } else if (segments.length >= 2 && "cases".equals(segments[0])) {
            String caseId = segments[1];
            resourceId = new ResourceId("CASE", caseId);
            if (segments.length >= 3 && "cancel".equals(segments[2])) {
                action = AclAction.ADMIN;
            } else if ("GET".equals(method)) {
                action = AclAction.READ;
            } else {
                action = AclAction.WRITE;
            }
        } else if ("cases".equals(segments[0]) && segments.length == 1 && "POST".equals(method)) {
            try {
                AccessControlProvider postAcl = jakarta.enterprise.inject.spi.CDI.current().select(AccessControlProvider.class).get();
                CurrentPrincipal postPrincipal = jakarta.enterprise.inject.spi.CDI.current().select(CurrentPrincipal.class).get();
                org.eclipse.microprofile.context.ManagedExecutor postMe =
                        jakarta.enterprise.inject.spi.CDI.current().select(org.eclipse.microprofile.context.ManagedExecutor.class).get();
                boolean hasAny = postMe.supplyAsync(() ->
                        !postAcl.accessibleResources(postPrincipal.actorId(), "CASE_DEFINITION", AclAction.WRITE).isEmpty())
                        .toCompletableFuture().get(5, java.util.concurrent.TimeUnit.SECONDS);
                if (!hasAny) {
                    ctx.abortWith(Response.status(403)
                            .entity(new ProblemDetail("Access denied", 403, "Insufficient permissions"))
                            .type(MediaType.APPLICATION_JSON_TYPE)
                            .build());
                }
            } catch (Exception e) {
                LOG.warnf(e, "ACL check failed for POST /api/v1/cases — allowing through");
            }
            return;
        } else {
            return;
        }

        try {
            AccessControlProvider acl       = jakarta.enterprise.inject.spi.CDI.current().select(AccessControlProvider.class).get();
            CurrentPrincipal      principal = jakarta.enterprise.inject.spi.CDI.current().select(CurrentPrincipal.class).get();
            ResourceId            rid       = resourceId;
            AclAction             act       = action;
            org.eclipse.microprofile.context.ManagedExecutor me =
                    jakarta.enterprise.inject.spi.CDI.current().select(org.eclipse.microprofile.context.ManagedExecutor.class).get();
            boolean allowed = me.supplyAsync(() -> acl.canAccess(principal.actorId(), rid, act))
                                .toCompletableFuture().get(5, java.util.concurrent.TimeUnit.SECONDS);
            if (!allowed) {
                ctx.abortWith(Response.status(403)
                                      .entity(new ProblemDetail("Access denied", 403, "Insufficient permissions"))
                                      .type(MediaType.APPLICATION_JSON_TYPE)
                                      .build());
            }
        } catch (Exception e) {
            LOG.warnf(e, "ACL check failed for %s %s — allowing through", ctx.getMethod(), path);
        }
    }
}
