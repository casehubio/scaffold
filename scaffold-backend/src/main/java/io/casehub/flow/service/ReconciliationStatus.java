package io.casehub.flow.service;

import java.util.List;

public record ReconciliationStatus(List<ClusterStatus> clusters) {}
