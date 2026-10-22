package io.casehub.flow.service.embedded;

import io.casehub.api.engine.rest.EngineCaseApi;
import io.casehub.api.model.CaseStatus;
import io.casehub.api.view.CaseInstanceView;
import io.casehub.flow.service.CaseDetail;
import io.casehub.flow.service.CaseService;
import io.casehub.flow.service.CaseSummary;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
@Alternative
@Priority(100)
public class EmbeddedCaseService implements CaseService {

    private final EngineCaseApi engineCaseApi;

    @Inject
    public EmbeddedCaseService(EngineCaseApi engineCaseApi) {
        this.engineCaseApi = engineCaseApi;
    }

    @Override
    public List<CaseSummary> listActiveCases(String tenancyId) {
        var page = engineCaseApi.listCases(CaseStatus.RUNNING, null, null, tenancyId, 0, 100);
        return page.items().stream().map(this::toSummary).toList();
    }

    @Override
    public CaseDetail getCaseDetail(UUID caseId, String tenancyId) {
        var instance = engineCaseApi.getCaseById(caseId, tenancyId);
        if (instance == null) return null;
        return new CaseDetail(instance.caseId(), instance.name(), instance.status().name(),
                instance.createdAt(), null, List.of(), List.of(), List.of());
    }

    private CaseSummary toSummary(CaseInstanceView view) {
        return new CaseSummary(view.caseId(), view.name(), view.status().name(),
                view.createdAt(), 0, null, null, null);
    }
}
