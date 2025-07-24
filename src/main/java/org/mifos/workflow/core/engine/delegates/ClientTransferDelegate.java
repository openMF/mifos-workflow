package org.mifos.workflow.core.engine.delegates;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.mifos.workflow.dto.fineract.client.ClientTransferRequestDTO;
import org.mifos.workflow.exception.FineractApiException;
import org.mifos.workflow.exception.WorkflowException;
import org.mifos.workflow.service.fineract.client.FineractClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Delegate for transferring a client in the Fineract system.
 */
@Component
public class ClientTransferDelegate implements JavaDelegate {

    private static final Logger logger = LoggerFactory.getLogger(ClientTransferDelegate.class);

    private final FineractClientService fineractClientService;

    @Autowired
    public ClientTransferDelegate(FineractClientService fineractClientService) {
        this.fineractClientService = fineractClientService;
    }

    @Override
    public void execute(DelegateExecution execution) {
        Long clientId = (Long) execution.getVariable("clientId");
        Long destinationOfficeId = (Long) execution.getVariable("destinationOfficeId");
        LocalDate effectiveDate = (LocalDate) execution.getVariable("effectiveDate");
        String dateFormat = (String) execution.getVariable("dateFormat");
        String locale = (String) execution.getVariable("locale");
        try {
            if (clientId == null) {
                throw new IllegalArgumentException("clientId is missing from process variables");
            }
            if (destinationOfficeId == null) {
                throw new IllegalArgumentException("destinationOfficeId is missing from process variables");
            }
            if (effectiveDate == null) {
                throw new IllegalArgumentException("effectiveDate is missing from process variables");
            }
            logger.info("Proposing transfer for client {} to office {} with effective date {}", clientId, destinationOfficeId, effectiveDate);
            ClientTransferRequestDTO transferRequest = ClientTransferRequestDTO.builder()
                    .destinationOfficeId(destinationOfficeId)
                    .transferDate(effectiveDate)
                    .dateFormat(dateFormat)
                    .locale(locale)
                    .build();
            fineractClientService.proposeClientTransfer(clientId, transferRequest).blockingFirst();
            execution.setVariable("transferProposed", true);
            execution.setVariable("transferStatus", "PROPOSED");
        } catch (FineractApiException e) {
            logger.error("Fineract API error during client transfer proposal: {}", e.getMessage());
            execution.setVariable("transferProposed", false);
            execution.setVariable("transferStatus", "ERROR");
            execution.setVariable("errorMessage", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error during client transfer proposal: {}", e.getMessage(), e);
            execution.setVariable("transferProposed", false);
            execution.setVariable("transferStatus", "ERROR");
            execution.setVariable("errorMessage", e.getMessage());
            throw new WorkflowException("Client transfer proposal failed", e, "client transfer proposal", "ERROR_CLIENT_TRANSFER_PROPOSAL_FAILED");
        }
    }
} 