package io.casehub.flow.service.client;

import io.casehub.flow.service.DeployResult;
import io.casehub.flow.service.TopologyCatalogueService;
import io.casehub.flow.service.TopologyEntry;
import io.casehub.flow.service.TopologyPlan;
import io.casehub.flow.service.client.api.ScaffoldCatalogueRestClient;
import io.quarkus.arc.DefaultBean;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.List;

@ApplicationScoped
@DefaultBean
public class ClientTopologyCatalogueService implements TopologyCatalogueService {

    private final ScaffoldCatalogueRestClient restClient;

    @Inject
    public ClientTopologyCatalogueService(@RestClient ScaffoldCatalogueRestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public List<TopologyEntry> listTopologies() {
        return restClient.listTopologies();
    }

    @Override
    public TopologyPlan getTopologyPlan(String name) {
        return restClient.getTopologyPlan(name);
    }

    @Override
    public DeployResult deploy(String name, String tenancyId) {
        return restClient.deploy(name);
    }
}
