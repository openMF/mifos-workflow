package org.mifos.workflow.core.engine.delegates;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.mifos.workflow.dto.fineract.client.ClientRejectTransferRequestDTO;
import org.mifos.workflow.exception.FineractApiException;
import org.mifos.workflow.exception.WorkflowException;
import org.mifos.workflow.service.fineract.client.FineractClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Delegate for rejecting a client transfer in the Fineract system.
 * This is a placeholder implementation.
 */
@Component
public class TransferRejectionDelegate implements JavaDelegate {

    private static final Logger logger = LoggerFactory.getLogger(TransferRejectionDelegate.class);

    private final FineractClientService fineractClientService;

    @Autowired
    public TransferRejectionDelegate(FineractClientService fineractClientService) {
        this.fineractClientService = fineractClientService;
    }

    @Override
    public void execute(DelegateExecution execution) {
        Long clientId = (Long) execution.getVariable("clientId");
        String rejectionReason = (String) execution.getVariable("rejectionReason");
        try {
            if (clientId == null) {
                throw new IllegalArgumentException("clientId is missing from process variables");
            }
            if (rejectionReason == null) {
                throw new IllegalArgumentException("rejectionReason is missing from process variables");
            }
            logger.info("Rejecting transfer for client {} with reason '{}'", clientId, rejectionReason);
            ClientRejectTransferRequestDTO rejectRequest = ClientRejectTransferRequestDTO.builder()
                .note(rejectionReason)
                .build();
            fineractClientService.rejectClientTransfer(clientId, "rejectTransfer", rejectRequest).blockingFirst();
            execution.setVariable("transferRejected", true);
            execution.setVariable("transferStatus", "REJECTED");
        } catch (FineractApiException e) {
            logger.error("Fineract API error during client transfer rejection: {}", e.getMessage());
            execution.setVariable("transferRejected", false);
            execution.setVariable("transferStatus", "ERROR");
            execution.setVariable("errorMessage", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error during client transfer rejection: {}", e.getMessage(), e);
            execution.setVariable("transferRejected", false);
            execution.setVariable("transferStatus", "ERROR");
            execution.setVariable("errorMessage", e.getMessage());
            throw new WorkflowException("Client transfer rejection failed", e, "client transfer rejection", "ERROR_CLIENT_TRANSFER_REJECTION_FAILED");
        }
    }
} 