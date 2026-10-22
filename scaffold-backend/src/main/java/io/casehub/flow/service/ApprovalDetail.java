package io.casehub.flow.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ApprovalDetail(
    UUID approvalId, String nodeId, String action,
    String risk, String summary, String cluster,
    String namespace, String status, String assigneeId,
    Instant createdAt, List<CbrMatch> precedents) {}
