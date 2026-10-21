package io.casehub.flow.service.client.api;

import java.util.List;

public record OpsTopologyResponse(List<OpsLoopTopologyDto> loops) {}
