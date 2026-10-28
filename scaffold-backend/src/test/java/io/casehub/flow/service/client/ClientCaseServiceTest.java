package io.casehub.flow.service.client;

import io.casehub.flow.service.client.api.EngineCaseRestClient;
import io.casehub.api.model.CaseStatus;
import io.casehub.api.view.CaseInstanceView;
import io.casehub.api.model.TaskStatus;
import io.casehub.api.view.CasePage;
import io.casehub.api.view.PlanItemView;
import io.casehub.flow.service.CaseDetail;
import io.casehub.flow.service.CaseSummary;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ClientCaseServiceTest {

    @Test
    void listActiveCasesDelegatesViaRestClient() {
        var restClient = mock(EngineCaseRestClient.class);
        var caseId = UUID.randomUUID();
        var view = new CaseInstanceView(caseId, CaseStatus.RUNNING,
                "ops", "deployment-approval", "1.0", Instant.now(), null);
        when(restClient.listCases(eq(CaseStatus.RUNNING), isNull(), isNull(),
                eq("t1"), eq(0), eq(100)))
                .thenReturn(new CasePage(List.of(view), 1, false));

        var service = new ClientCaseService(restClient);
        List<CaseSummary> result = service.listActiveCases("t1");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).caseType()).isEqualTo("deployment-approval");
    }

    @Test
    void getCaseDetailDelegatesViaRestClientWithSubCases() {
        var restClient = mock(EngineCaseRestClient.class);
        var caseId = UUID.randomUUID();
        var subCaseId = UUID.randomUUID();
        var view = new CaseInstanceView(caseId, CaseStatus.RUNNING,
                "scaffold", "compliance-audit", "1.0.0", Instant.now(), null);
        var planItem = new PlanItemView(UUID.randomUUID(), "delegate-to-ops",
                TaskStatus.RUNNING, "CaseTask", null, "compliance-check", List.of(subCaseId));
        when(restClient.getCaseById(caseId, "t1")).thenReturn(view);
        when(restClient.getPlanItems(caseId, "t1")).thenReturn(List.of(planItem));

        var service = new ClientCaseService(restClient);
        CaseDetail detail = service.getCaseDetail(caseId, "t1");

        assertThat(detail).isNotNull();
        assertThat(detail.caseId()).isEqualTo(caseId);
        assertThat(detail.subCases()).hasSize(1);
        assertThat(detail.subCases().get(0).caseId()).isEqualTo(subCaseId);
        assertThat(detail.subCases().get(0).caseType()).isEqualTo("compliance-check");
    }

    @Test
    void getCaseDetailReturnsNullForMissingCase() {
        var restClient = mock(EngineCaseRestClient.class);
        var caseId = UUID.randomUUID();
        when(restClient.getCaseById(caseId, "t1")).thenReturn(null);

        var service = new ClientCaseService(restClient);
        assertThat(service.getCaseDetail(caseId, "t1")).isNull();
    }
}
