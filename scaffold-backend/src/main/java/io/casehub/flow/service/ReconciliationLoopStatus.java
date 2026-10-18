package io.casehub.flow.service;

public record ReconciliationLoopStatus(String loopId, String tenancyId, String state, int nodeCount) {}
