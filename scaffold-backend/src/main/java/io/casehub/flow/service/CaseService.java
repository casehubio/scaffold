package io.casehub.flow.service;

import java.util.List;
import java.util.UUID;

public interface CaseService {
    List<CaseSummary> listActiveCases(String tenancyId);
    CaseDetail getCaseDetail(UUID caseId, String tenancyId);
}
