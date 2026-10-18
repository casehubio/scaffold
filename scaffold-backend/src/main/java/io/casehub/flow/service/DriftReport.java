package io.casehub.flow.service;

import java.util.List;

public record DriftReport(String loopId, List<DriftEntry> entries) {

    public record DriftEntry(String nodeId, String expected, String actual) {}
}
