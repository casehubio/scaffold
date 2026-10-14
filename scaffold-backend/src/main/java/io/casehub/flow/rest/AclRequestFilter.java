package io.casehub.flow.rest;

import io.casehub.engine.rest.dto.ProblemDetail;
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
import org.jboss.logging.Logger;

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

        if (!path.startsWith("api/engine/")) {return;}

        String   method   = ctx.getMethod();
        String[] segments = path.substring("api/engine/".length()).split("/");
        for (int i = 0; i < segments.length; i++) {
            segments[i] = java.net.URLDecoder.decode(segments[i], java.nio.charset.StandardCharsets.UTF_8);
        }

        ResourceId resourceId;
        AclAction  action;

        if ("definitions".equals(segments[0])) {
            if (segments.length >= 4 && "get-definitions-by-name".equals(segments[1])) {
                resourceId = new ResourceId(io.casehub.api.acl.EngineResourceTypes.CASE_DEFINITION, segments[2] + "/" + segments[3]);
                action     = AclAction.READ;
            } else if (segments.length >= 5 && "get-definition-by-key".equals(segments[1])) {
                resourceId = new ResourceId(io.casehub.api.acl.EngineResourceTypes.CASE_DEFINITION, segments[2] + "/" + segments[3]);
                action     = AclAction.READ;
            } else {
                return;
            }
        } else if ("cases".equals(segments[0])) {
            if (segments.length >= 2 && "start-case".equals(segments[1]) && "POST".equals(method)) {
                try {
                    AccessControlProvider postAcl       = jakarta.enterprise.inject.spi.CDI.current().select(AccessControlProvider.class).get();
                    CurrentPrincipal      postPrincipal = jakarta.enterprise.inject.spi.CDI.current().select(CurrentPrincipal.class).get();
                    String                postActorId   = postPrincipal.actorId();
                    org.eclipse.microprofile.context.ManagedExecutor postMe =
                            jakarta.enterprise.inject.spi.CDI.current().select(org.eclipse.microprofile.context.ManagedExecutor.class).get();
                    boolean hasAny = postMe.supplyAsync(() ->
                                                                !postAcl.accessibleResources(postActorId, io.casehub.api.acl.EngineResourceTypes.CASE_DEFINITION, AclAction.WRITE).isEmpty())
                                           .toCompletableFuture().get(5, java.util.concurrent.TimeUnit.SECONDS);
                    if (!hasAny) {
                        ctx.abortWith(Response.status(403)
                                              .entity(new ProblemDetail("Access denied", 403, "Insufficient permissions"))
                                              .type(MediaType.APPLICATION_JSON_TYPE)
                                              .build());
                    }
                } catch (Exception e) {
                    LOG.warnf(e, "ACL check failed for POST start-case — allowing through");
                }
                return;
            } else if (segments.length >= 3) {
                String caseId = segments[2];
                resourceId = new ResourceId(io.casehub.api.acl.EngineResourceTypes.CASE, caseId);
                action     = AclAction.READ;
            } else {
                return;
            }
        } else if ("control".equals(segments[0]) && segments.length >= 3) {
            String operation = segments[1];
            String caseId    = segments[2];
            resourceId = new ResourceId(io.casehub.api.acl.EngineResourceTypes.CASE, caseId);
            if ("cancel-case".equals(operation)) {
                action = AclAction.ADMIN;
            } else {
                action = AclAction.WRITE;
            }
        } else if ("events".equals(segments[0]) && segments.length >= 3 && "get-event-log".equals(segments[1])) {
            String caseId = segments[2];
            resourceId = new ResourceId(io.casehub.api.acl.EngineResourceTypes.CASE, caseId);
            action     = AclAction.READ;
        } else {
            return;
        }

        try {
            AccessControlProvider acl       = jakarta.enterprise.inject.spi.CDI.current().select(AccessControlProvider.class).get();
            CurrentPrincipal      principal = jakarta.enterprise.inject.spi.CDI.current().select(CurrentPrincipal.class).get();
            String                actorId   = principal.actorId();
            ResourceId            rid       = resourceId;
            AclAction             act       = action;

            org.eclipse.microprofile.context.ManagedExecutor me =
                    jakarta.enterprise.inject.spi.CDI.current().select(org.eclipse.microprofile.context.ManagedExecutor.class).get();
            boolean allowed = me.supplyAsync(() -> acl.canAccess(actorId, rid, act))
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
