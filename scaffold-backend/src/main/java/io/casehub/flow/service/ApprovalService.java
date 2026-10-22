package io.casehub.flow.service;

import java.util.List;
import java.util.UUID;

public interface ApprovalService {
    List<ApprovalSummary> listPendingApprovals(String tenancyId);
    ApprovalDetail getApproval(UUID approvalId);
    void approve(UUID approvalId, String actorId);
    void reject(UUID approvalId, String actorId, String reason);
}
