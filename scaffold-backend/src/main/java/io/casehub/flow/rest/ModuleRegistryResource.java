package io.casehub.flow.rest;

import io.casehub.flow.app.AppProducer;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Path("/api/modules")
@Produces(MediaType.APPLICATION_JSON)
public class ModuleRegistryResource {

    private final List<String> modules;

    @Inject
    public ModuleRegistryResource(AppProducer appProducer) {
        var appConfig = appProducer.getConfig();
        if (appConfig.isPresent()) {
            var config = appConfig.get();
            var active = new ArrayList<>(config.modules().required());
            for (var opt : config.modules().optional()) {
                if (probeClass(resolveProbeClass(opt))) {
                    active.add(opt);
                }
            }
            this.modules = List.copyOf(active);
        } else {
            var detected = new ArrayList<String>();
            probeModule("io.casehub.api.engine.CaseHubRuntime", "engine", detected);
            probeModule("io.casehub.work.runtime.service.WorkItemService", "work", detected);
            probeModule("io.casehub.engine.plan.DagPlan", "planning", detected);
            probeModule("io.casehub.ledger.LedgerService", "ledger", detected);
            probeModule("io.casehub.ops.app.OpsApplication", "ops", detected);
            probeModule("io.casehub.iot.webapp.IoTApplication", "iot", detected);
            this.modules = List.copyOf(detected);
        }
    }

    @GET
    public Map<String, List<String>> getModules() {
        return Map.of("modules", modules);
    }

    private static String resolveProbeClass(String moduleName) {
        return switch (moduleName) {
            case "engine" -> "io.casehub.api.engine.CaseHubRuntime";
            case "work" -> "io.casehub.work.runtime.service.WorkItemService";
            case "planning" -> "io.casehub.engine.plan.DagPlan";
            case "ledger" -> "io.casehub.ledger.LedgerService";
            case "ops" -> "io.casehub.ops.app.OpsApplication";
            case "iot" -> "io.casehub.iot.webapp.IoTApplication";
            case "ops-compliance" -> "io.casehub.ops.compliance.ComplianceGoalCompiler";
            case "ops-deployment" -> "io.casehub.ops.deployment.DeploymentGoalCompiler";
            case "desiredstate" -> "io.casehub.desiredstate.api.GoalCompiler";
            case "ops-service" -> "io.casehub.ops.service.api.OpsApplicationApi";
            case "qhorus" -> "io.casehub.qhorus.api.QhorusChannel";
            case "eidos" -> "io.casehub.eidos.api.AgentDescriptor";
            default -> moduleName;
        };
    }

    private static boolean probeClass(String className) {
        try {
            Class.forName(className, false, Thread.currentThread().getContextClassLoader());
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    private static void probeModule(String className, String moduleName, List<String> target) {
        if (probeClass(className)) {
            target.add(moduleName);
        }
    }
}
