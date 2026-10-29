package io.casehub.flow.service;

import java.util.UUID;

public record DeployResult(UUID caseId, String loopKey) {}
