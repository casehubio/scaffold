package io.casehub.flow.service.client;

import io.casehub.api.model.CaseStatus;
import io.casehub.flow.service.CaseDetail;
import io.casehub.flow.service.CaseService;
import io.casehub.flow.service.CaseSummary;
import io.casehub.flow.service.SubCaseSummary;
import io.casehub.flow.service.client.api.EngineCaseRestClient;
import io.quarkus.arc.DefaultBean;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
@DefaultBean
public class ClientCaseService implements CaseService {

    private final EngineCaseRestClient restClient;

    @Inject
    public ClientCaseService(@RestClient EngineCaseRestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public List<CaseSummary> listActiveCases(String tenancyId) {
        var page = restClient.listCases(CaseStatus.RUNNING, null, null, tenancyId, 0, 100);
        return page.items().stream()
                .map(v -> new CaseSummary(v.caseId(), v.name(), v.status().name(),
                        v.createdAt(), 0, null, null, null))
                .toList();
    }

    @Override
    public CaseDetail getCaseDetail(UUID caseId, String tenancyId) {
        var instance = restClient.getCaseById(caseId, tenancyId);
        if (instance == null) return null;
        var planItems = restClient.getPlanItems(caseId, tenancyId);
        var subCases = planItems.stream()
                .filter(pi -> pi.subCaseIds() != null && !pi.subCaseIds().isEmpty())
                .flatMap(pi -> pi.subCaseIds().stream()
                        .map(subId -> new SubCaseSummary(subId, pi.capabilityName(), pi.status().name())))
                .toList();
        return new CaseDetail(instance.caseId(), instance.name(), instance.status().name(),
                instance.createdAt(), null, subCases, List.of(), List.of());
    }
}
