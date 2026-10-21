package io.casehub.flow.service;

public record TopologyNode(
    String nodeId, String nodeType, String targetStatus,
    String actualStatus, String loopKey, boolean adapted) {}
