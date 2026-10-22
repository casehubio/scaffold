package io.casehub.flow.service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record CaseDetail(
    UUID caseId, String caseType, String status,
    Instant createdAt, Map<String, Object> context,
    List<SubCaseSummary> subCases,
    List<CaseEvent> events,
    List<MilestoneStatus> milestones) {}
