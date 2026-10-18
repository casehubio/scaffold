package io.casehub.flow.service;

import java.util.List;
import java.util.Map;

public record TopologySnapshot(List<LoopEntry> loops) {

    public record LoopEntry(
        String loopId,
        String tenancyId,
        List<NodeEntry> desiredNodes,
        Map<String, String> actualStatuses
    ) {}

    public record NodeEntry(
        String nodeId,
        String type,
        Map<String, Object> spec
    ) {}
}
