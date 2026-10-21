package io.casehub.flow.service.client.api;

public record OpsTopologyNodeDto(String nodeId, String nodeType,
        String targetStatus, boolean adapted) {}
