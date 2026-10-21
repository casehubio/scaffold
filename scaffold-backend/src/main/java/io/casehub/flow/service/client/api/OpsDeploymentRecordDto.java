package io.casehub.flow.service.client.api;

import java.time.Instant;

public record OpsDeploymentRecordDto(String id, String outcome, Instant createdAt) {}
