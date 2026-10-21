package io.casehub.flow.service;

public interface TopologyService {
    TopologySnapshot getSnapshot(String tenancyId);
}
