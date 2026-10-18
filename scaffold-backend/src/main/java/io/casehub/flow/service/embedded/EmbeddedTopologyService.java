package io.casehub.flow.service.embedded;

import io.casehub.flow.service.TopologyService;
import io.casehub.flow.service.TopologySnapshot;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;

import java.util.List;

@ApplicationScoped
@Alternative
@Priority(100)
public class EmbeddedTopologyService implements TopologyService {

    @Override
    public TopologySnapshot getSnapshot() {
        return new TopologySnapshot(List.of());
    }
}
