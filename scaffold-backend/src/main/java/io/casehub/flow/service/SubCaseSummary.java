package io.casehub.flow.service;

import java.util.UUID;

public record SubCaseSummary(UUID caseId, String caseType, String status) {}
