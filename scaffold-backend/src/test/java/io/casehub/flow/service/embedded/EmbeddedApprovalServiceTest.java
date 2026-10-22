package io.casehub.flow.service.embedded;

import io.casehub.ops.api.approval.RiskClassification;
import io.casehub.ops.service.api.OpsApprovalApi;
import io.casehub.ops.service.rest.ApprovalView;
import io.casehub.flow.service.ApprovalSummary;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class EmbeddedApprovalServiceTest {

    @Test
    void listPendingApprovalsMapsFromOpsApprovalView() {
        var opsApi = mock(OpsApprovalApi.class);
        var id = UUID.randomUUID();
        var view = new ApprovalView(id, "node-1", "PROVISION",
                RiskClassification.HIGH, "Deploy to prod", null, null,
                "PENDING", null, Instant.now());
        when(opsApi.listApprovals()).thenReturn(List.of(view));

        var service = new EmbeddedApprovalService(opsApi);
        List<ApprovalSummary> result = service.listPendingApprovals("tenant-1");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).approvalId()).isEqualTo(id);
        assertThat(result.get(0).risk()).isEqualTo("HIGH");
        assertThat(result.get(0).action()).isEqualTo("PROVISION");
    }

    @Test
    void approveDelegatesToOpsApi() {
        var opsApi = mock(OpsApprovalApi.class);
        var id = UUID.randomUUID();

        var service = new EmbeddedApprovalService(opsApi);
        service.approve(id, "actor-1");

        verify(opsApi).approve(id, "actor-1");
    }

    @Test
    void rejectDelegatesToOpsApi() {
        var opsApi = mock(OpsApprovalApi.class);
        var id = UUID.randomUUID();

        var service = new EmbeddedApprovalService(opsApi);
        service.reject(id, "actor-1", "Too risky");

        verify(opsApi).reject(id, "actor-1", "Too risky");
    }
}
