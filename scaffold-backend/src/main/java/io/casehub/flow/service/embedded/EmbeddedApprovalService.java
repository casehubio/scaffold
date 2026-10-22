package io.casehub.flow.service.embedded;

import io.casehub.flow.service.ApprovalDetail;
import io.casehub.flow.service.ApprovalService;
import io.casehub.flow.service.ApprovalSummary;
import io.casehub.ops.service.api.OpsApprovalApi;
import io.casehub.ops.service.rest.ApprovalView;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
@Alternative
@Priority(100)
public class EmbeddedApprovalService implements ApprovalService {

    private final OpsApprovalApi opsApprovalApi;

    @Inject
    public EmbeddedApprovalService(OpsApprovalApi opsApprovalApi) {
        this.opsApprovalApi = opsApprovalApi;
    }

    @Override
    public List<ApprovalSummary> listPendingApprovals(String tenancyId) {
        return opsApprovalApi.listApprovals().stream().map(this::toSummary).toList();
    }

    @Override
    public ApprovalDetail getApproval(UUID approvalId) {
        var view = opsApprovalApi.getApproval(approvalId);
        if (view == null) return null;
        return new ApprovalDetail(view.workItemId(), view.nodeId(), view.action(),
                view.risk() != null ? view.risk().name() : null, view.summary(),
                view.cluster(), view.namespace(), view.status(),
                view.assigneeId(), view.createdAt(), List.of());
    }

    @Override
    public void approve(UUID approvalId, String actorId) {
        opsApprovalApi.approve(approvalId, actorId);
    }

    @Override
    public void reject(UUID approvalId, String actorId, String reason) {
        opsApprovalApi.reject(approvalId, actorId, reason);
    }

    private ApprovalSummary toSummary(ApprovalView view) {
        return new ApprovalSummary(view.workItemId(), view.nodeId(), view.action(),
                view.risk() != null ? view.risk().name() : null, view.summary(),
                view.status(), view.createdAt());
    }
}
