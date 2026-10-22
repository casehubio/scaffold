package io.casehub.flow.service;

import java.time.Instant;
import java.util.UUID;

public record ApprovalSummary(
    UUID approvalId, String nodeId, String action,
    String risk, String summary, String status,
    Instant createdAt) {}
