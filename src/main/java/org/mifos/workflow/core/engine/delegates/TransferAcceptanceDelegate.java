package org.mifos.workflow.core.engine.delegates;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.mifos.workflow.dto.fineract.client.ClientAcceptTransferRequestDTO;
import org.mifos.workflow.exception.FineractApiException;
import org.mifos.workflow.exception.WorkflowException;
import org.mifos.workflow.service.fineract.client.FineractClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Delegate for accepting a client transfer in the Fineract system.
 */
@Component
public class TransferAcceptanceDelegate implements JavaDelegate {

    private static final Logger logger = LoggerFactory.getLogger(TransferAcceptanceDelegate.class);

    private final FineractClientService fineractClientService;

    @Autowired
    public TransferAcceptanceDelegate(FineractClientService fineractClientService) {
        this.fineractClientService = fineractClientService;
    }

    @Override
    public void execute(DelegateExecution execution) {
        Long clientId = (Long) execution.getVariable("clientId");
        LocalDate transferDate = (LocalDate) execution.getVariable("effectiveDate");
        try {
            if (clientId == null) {
                throw new IllegalArgumentException("clientId is missing from process variables");
            }
            if (transferDate == null) {
                throw new IllegalArgumentException("effectiveDate is missing from process variables");
            }
            logger.info("Accepting transfer for client {} with effective date {}", clientId, transferDate);
            ClientAcceptTransferRequestDTO acceptRequest = ClientAcceptTransferRequestDTO.builder().transferDate(transferDate).build();
            fineractClientService.acceptClientTransfer(clientId, "acceptTransfer", acceptRequest).blockingFirst();
            execution.setVariable("transferAccepted", true);
            execution.setVariable("transferStatus", "ACCEPTED");
        } catch (FineractApiException e) {
            logger.error("Fineract API error during client transfer acceptance: {}", e.getMessage());
            execution.setVariable("transferAccepted", false);
            execution.setVariable("transferStatus", "ERROR");
            execution.setVariable("errorMessage", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error during client transfer acceptance: {}", e.getMessage(), e);
            execution.setVariable("transferAccepted", false);
            execution.setVariable("transferStatus", "ERROR");
            execution.setVariable("errorMessage", e.getMessage());
            throw new WorkflowException("Client transfer acceptance failed", e, "client transfer acceptance", "ERROR_CLIENT_TRANSFER_ACCEPTANCE_FAILED");
        }
    }
} 