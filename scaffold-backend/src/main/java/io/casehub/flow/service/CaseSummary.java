package io.casehub.flow.service;

import java.time.Instant;
import java.util.UUID;

public record CaseSummary(
    UUID caseId, String caseType, String status,
    Instant createdAt, int subCaseCount,
    String escalationState, UUID applicationId,
    String nodeId) {}
