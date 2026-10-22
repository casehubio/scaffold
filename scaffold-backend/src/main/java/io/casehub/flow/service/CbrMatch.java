package io.casehub.flow.service;

import java.util.UUID;

public record CbrMatch(UUID caseId, String decision, double similarity, String summary) {}
