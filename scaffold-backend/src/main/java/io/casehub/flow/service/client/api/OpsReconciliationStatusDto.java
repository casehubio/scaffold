package io.casehub.flow.service.client.api;

import java.util.List;

public record OpsReconciliationStatusDto(List<OpsClusterStatusDto> clusters) {}
