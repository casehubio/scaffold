package io.casehub.flow.service.client;

import io.casehub.flow.service.ApprovalDetail;
import io.casehub.flow.service.ApprovalService;
import io.casehub.flow.service.ApprovalSummary;
import io.casehub.flow.service.client.api.OpsApprovalRestClient;
import io.casehub.ops.service.rest.ApprovalView;
import io.quarkus.arc.DefaultBean;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
@DefaultBean
public class ClientApprovalService implements ApprovalService {

    private final OpsApprovalRestClient restClient;

    @Inject
    public ClientApprovalService(@RestClient OpsApprovalRestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public List<ApprovalSummary> listPendingApprovals(String tenancyId) {
        return restClient.listApprovals().stream().map(this::toSummary).toList();
    }

    @Override
    public ApprovalDetail getApproval(UUID approvalId) {
        var view = restClient.getApproval(approvalId);
        if (view == null) return null;
        return new ApprovalDetail(view.workItemId(), view.nodeId(), view.action(),
                view.risk() != null ? view.risk().name() : null, view.summary(),
                view.cluster(), view.namespace(), view.status(),
                view.assigneeId(), view.createdAt(), List.of());
    }

    @Override
    public void approve(UUID approvalId, String actorId) {
        restClient.approve(approvalId, actorId);
    }

    @Override
    public void reject(UUID approvalId, String actorId, String reason) {
        restClient.reject(approvalId, actorId, reason);
    }

    private ApprovalSummary toSummary(ApprovalView view) {
        return new ApprovalSummary(view.workItemId(), view.nodeId(), view.action(),
                view.risk() != null ? view.risk().name() : null, view.summary(),
                view.status(), view.createdAt());
    }
}
