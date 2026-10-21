package io.casehub.flow.service;

import java.time.Instant;

public record DeploymentRecord(
    String deploymentId, String status, String message,
    Instant createdAt) {}
