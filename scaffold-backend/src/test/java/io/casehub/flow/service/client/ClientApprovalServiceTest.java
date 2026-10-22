package io.casehub.flow.service.client;

import io.casehub.flow.service.client.api.OpsApprovalRestClient;
import io.casehub.ops.api.approval.RiskClassification;
import io.casehub.ops.service.rest.ApprovalView;
import io.casehub.flow.service.ApprovalSummary;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ClientApprovalServiceTest {

    @Test
    void listPendingApprovalsDelegatesViaRestClient() {
        var restClient = mock(OpsApprovalRestClient.class);
        var id = UUID.randomUUID();
        var view = new ApprovalView(id, "node-1", "PROVISION",
                RiskClassification.HIGH, "Deploy to prod", null, null,
                "PENDING", null, Instant.now());
        when(restClient.listApprovals()).thenReturn(List.of(view));

        var service = new ClientApprovalService(restClient);
        List<ApprovalSummary> result = service.listPendingApprovals("t1");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).approvalId()).isEqualTo(id);
        assertThat(result.get(0).risk()).isEqualTo("HIGH");
    }

    @Test
    void approveDelegatesToRestClient() {
        var restClient = mock(OpsApprovalRestClient.class);
        var id = UUID.randomUUID();

        var service = new ClientApprovalService(restClient);
        service.approve(id, "actor-1");

        verify(restClient).approve(id, "actor-1");
    }

    @Test
    void rejectDelegatesToRestClient() {
        var restClient = mock(OpsApprovalRestClient.class);
        var id = UUID.randomUUID();

        var service = new ClientApprovalService(restClient);
        service.reject(id, "actor-1", "Too risky");

        verify(restClient).reject(id, "actor-1", "Too risky");
    }
}
