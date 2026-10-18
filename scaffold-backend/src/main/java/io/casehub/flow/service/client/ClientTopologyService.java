package io.casehub.flow.service.client;

import io.casehub.flow.service.TopologyService;
import io.casehub.flow.service.TopologySnapshot;
import io.quarkus.arc.DefaultBean;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
@DefaultBean
public class ClientTopologyService implements TopologyService {

    @Override
    public TopologySnapshot getSnapshot() {
        return new TopologySnapshot(List.of());
    }
}
