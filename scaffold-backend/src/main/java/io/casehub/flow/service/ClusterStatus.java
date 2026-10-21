package io.casehub.flow.service;

public record ClusterStatus(
    String clusterId, String clusterName,
    boolean active, int nodeCount) {}
