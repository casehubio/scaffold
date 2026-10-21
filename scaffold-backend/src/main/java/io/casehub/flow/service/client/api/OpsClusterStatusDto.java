package io.casehub.flow.service.client.api;

public record OpsClusterStatusDto(String clusterId, String clusterName,
        boolean active, int nodeCount) {}
