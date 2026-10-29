package io.casehub.flow.service;

import java.util.List;

public interface TopologyCatalogueService {
    List<TopologyEntry> listTopologies();
    TopologyPlan getTopologyPlan(String name);
    DeployResult deploy(String name, String tenancyId);
}
