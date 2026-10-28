package io.casehub.flow.service.embedded;

import io.casehub.api.engine.rest.EngineCaseApi;
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

class EmbeddedCaseServiceTest {

    @Test
    void listActiveCasesMapsFromEngineCasePage() {
        var engineApi = mock(EngineCaseApi.class);
        var caseId = UUID.randomUUID();
        var view = new CaseInstanceView(caseId, CaseStatus.RUNNING,
                "ops", "drift-remediation", "1.0", Instant.now(), null);
        when(engineApi.listCases(eq(CaseStatus.RUNNING), isNull(), isNull(),
                eq("tenant-1"), eq(0), eq(100)))
                .thenReturn(new CasePage(List.of(view), 1, false));

        var service = new EmbeddedCaseService(engineApi);
        List<CaseSummary> result = service.listActiveCases("tenant-1");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).caseId()).isEqualTo(caseId);
        assertThat(result.get(0).caseType()).isEqualTo("drift-remediation");
        assertThat(result.get(0).status()).isEqualTo("RUNNING");
    }

    @Test
    void getCaseDetailMapsFromEngineViewWithSubCases() {
        var engineApi = mock(EngineCaseApi.class);
        var caseId = UUID.randomUUID();
        var subCaseId = UUID.randomUUID();
        var view = new CaseInstanceView(caseId, CaseStatus.RUNNING,
                "scaffold", "deployment-approval", "1.0.0", Instant.now(), null);
        var planItem = new PlanItemView(UUID.randomUUID(), "delegate-to-ops",
                TaskStatus.RUNNING, "CaseTask", null, "deploy-approval", List.of(subCaseId));
        when(engineApi.getCaseById(caseId, "tenant-1")).thenReturn(view);
        when(engineApi.getPlanItems(caseId, "tenant-1")).thenReturn(List.of(planItem));

        var service = new EmbeddedCaseService(engineApi);
        CaseDetail detail = service.getCaseDetail(caseId, "tenant-1");

        assertThat(detail).isNotNull();
        assertThat(detail.caseId()).isEqualTo(caseId);
        assertThat(detail.caseType()).isEqualTo("deployment-approval");
        assertThat(detail.subCases()).hasSize(1);
        assertThat(detail.subCases().get(0).caseId()).isEqualTo(subCaseId);
        assertThat(detail.subCases().get(0).caseType()).isEqualTo("deploy-approval");
        assertThat(detail.subCases().get(0).status()).isEqualTo("RUNNING");
    }

    @Test
    void getCaseDetailReturnsNullForMissingCase() {
        var engineApi = mock(EngineCaseApi.class);
        var caseId = UUID.randomUUID();
        when(engineApi.getCaseById(caseId, "tenant-1")).thenReturn(null);

        var service = new EmbeddedCaseService(engineApi);
        assertThat(service.getCaseDetail(caseId, "tenant-1")).isNull();
    }
}
