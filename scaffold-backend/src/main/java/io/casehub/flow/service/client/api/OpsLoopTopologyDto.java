package io.casehub.flow.service.client.api;

import java.util.List;

public record OpsLoopTopologyDto(String loopKey,
        List<OpsTopologyNodeDto> desiredNodes,
        List<OpsNodeStatusDto> actualStatuses) {}
