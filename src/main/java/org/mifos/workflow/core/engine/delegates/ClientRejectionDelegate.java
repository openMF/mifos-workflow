package org.mifos.workflow.core.engine.delegates;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.mifos.fineract.client.models.PostClientsClientIdResponse;
import org.mifos.workflow.dto.fineract.client.ClientRejectRequestDTO;
import org.mifos.workflow.service.fineract.client.FineractClientService;
import org.mifos.workflow.exception.FineractApiException;
import org.mifos.workflow.exception.WorkflowException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Delegate for rejecting a client in the Fineract system.
 * Rejects a client application with a specified reason.
 */
@Component
public class ClientRejectionDelegate implements JavaDelegate {
    private static final Logger logger = LoggerFactory.getLogger(ClientRejectionDelegate.class);
    private static final String REJECT_COMMAND = "reject";
    private static final Long DEFAULT_REJECTION_REASON_ID = 1L;
    private static final String DEFAULT_DATE_FORMAT = "dd MMMM yyyy";
    private static final String DEFAULT_LOCALE = "en";
    
    private final FineractClientService fineractClientService;

    @Autowired
    public ClientRejectionDelegate(FineractClientService fineractClientService) {
        this.fineractClientService = fineractClientService;
    }

    @Override
    public void execute(DelegateExecution execution) {
        logger.info("ClientRejectionDelegate.execute() called for process instance: {}", execution.getProcessInstanceId());
        Long clientId = (Long) execution.getVariable("clientId");
        if (clientId == null) {
            throw new IllegalArgumentException("clientId is missing from process variables");
        }
        Object rejectionReasonObj = execution.getVariable("rejectionReason");
        String rejectionReason = null;
        if (rejectionReasonObj != null) {
            rejectionReason = rejectionReasonObj.toString();
        }
        if (rejectionReason == null || rejectionReason.trim().isEmpty()) {
            rejectionReason = "Application rejected during review process";
        }
        Object rejectionDateObj = execution.getVariable("rejectionDate");
        LocalDate rejectionDate = null;
        if (rejectionDateObj != null) {
            if (rejectionDateObj instanceof LocalDate) {
                rejectionDate = (LocalDate) rejectionDateObj;
            } else if (rejectionDateObj instanceof String) {
                String rejectionDateStr = (String) rejectionDateObj;
                if (!rejectionDateStr.trim().isEmpty()) {
                    try {
                        rejectionDate = LocalDate.parse(rejectionDateStr);
                    } catch (Exception e) {
                        logger.warn("Could not parse rejectionDate string: {}, using current date", rejectionDateStr);
                    }
                }
            } else {
                logger.warn("Unexpected rejectionDate type: {}, using current date", rejectionDateObj.getClass().getSimpleName());
            }
        }
        if (rejectionDate == null) {
            rejectionDate = LocalDate.now();
        }
        Object rejectionReasonIdObj = execution.getVariable("rejectionReasonId");
        Long rejectionReasonId = null;
        if (rejectionReasonIdObj != null) {
            if (rejectionReasonIdObj instanceof Long) {
                rejectionReasonId = (Long) rejectionReasonIdObj;
            } else if (rejectionReasonIdObj instanceof Integer) {
                rejectionReasonId = ((Integer) rejectionReasonIdObj).longValue();
            } else if (rejectionReasonIdObj instanceof String) {
                try {
                    rejectionReasonId = Long.parseLong((String) rejectionReasonIdObj);
                } catch (NumberFormatException e) {
                    logger.warn("Invalid rejection reason ID format: {}", rejectionReasonIdObj);
                }
            } else if (rejectionReasonIdObj instanceof Number) {
                rejectionReasonId = ((Number) rejectionReasonIdObj).longValue();
            }
        }
        if (rejectionReasonId == null) {
            rejectionReasonId = DEFAULT_REJECTION_REASON_ID;
            logger.warn("No rejection reason ID provided, using default: {}", rejectionReasonId);
        }
        logger.info("Rejecting client with ID: {} for reason: {}", clientId, rejectionReason);
        try {
            ClientRejectRequestDTO rejectRequest = ClientRejectRequestDTO.builder()
                    .rejectionDate(rejectionDate)
                    .rejectionReasonId(rejectionReasonId)
                    .dateFormat(DEFAULT_DATE_FORMAT)
                    .locale(DEFAULT_LOCALE)
                    .build();
            PostClientsClientIdResponse response = fineractClientService.rejectClient(
                    clientId,
                    REJECT_COMMAND,
                    rejectRequest
            ).blockingFirst();
            if (response != null && response.getResourceId() != null) {
                execution.setVariable("clientRejected", true);
                execution.setVariable("clientStatus", "REJECTED");
                execution.setVariable("rejectionDate", rejectionDate);
                execution.setVariable("rejectionReason", rejectionReason);
                logger.info("Successfully rejected client with ID: {}", clientId);
            } else {
                throw new RuntimeException("Failed to reject client: No response received");
            }
        } catch (FineractApiException e) {
            if (e.isNotFound() && (e.getErrorBody().contains("ClientRejectReason") || e.getErrorBody().contains("does not exist"))) {
                logger.warn("Rejection reason ID {} does not exist. Marking client as rejected without Fineract API call.", rejectionReasonId);
                execution.setVariable("clientRejected", true);
                execution.setVariable("clientStatus", "REJECTED");
                execution.setVariable("rejectionDate", rejectionDate);
                execution.setVariable("rejectionReason", rejectionReason);
                execution.setVariable("errorMessage", "Rejection reason ID " + rejectionReasonId + " does not exist, but client marked as rejected");
                return;
            }
            logger.error("Fineract API error during client rejection: {}", e.getMessage());
            execution.setVariable("clientRejected", false);
            execution.setVariable("errorMessage", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error rejecting client: {}", e.getMessage(), e);
            if (e.getMessage() != null && e.getMessage().contains("ClientRejectReason")) {
                logger.warn("Rejection reason not found. Marking client as rejected without Fineract API call.");
                execution.setVariable("clientRejected", true);
                execution.setVariable("clientStatus", "REJECTED");
                execution.setVariable("rejectionDate", rejectionDate);
                execution.setVariable("rejectionReason", rejectionReason);
                execution.setVariable("errorMessage", "Rejection reason not found, but client marked as rejected");
                return;
            }
            execution.setVariable("clientRejected", false);
            execution.setVariable("errorMessage", e.getMessage());
            throw new WorkflowException("Client rejection failed", e, "client rejection", "CLIENT_REJECTION_FAILED");
        }
    }
} 