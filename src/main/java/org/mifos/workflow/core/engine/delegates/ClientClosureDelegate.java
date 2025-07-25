package org.mifos.workflow.core.engine.delegates;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.mifos.workflow.dto.fineract.client.ClientCloseRequestDTO;
import org.mifos.workflow.exception.WorkflowException;
import org.mifos.workflow.service.fineract.client.FineractClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * Delegate for closing a client in the Fineract system.
 */
@Component
public class ClientClosureDelegate implements JavaDelegate {

    private static final Logger logger = LoggerFactory.getLogger(ClientClosureDelegate.class);
    private final FineractClientService fineractClientService;

    @Autowired
    public ClientClosureDelegate(FineractClientService fineractClientService) {
        this.fineractClientService = fineractClientService;
    }

    @Override
    public void execute(DelegateExecution execution) {
        Long clientId = (Long) execution.getVariable("clientId");
        Object closureReasonIdObj = execution.getVariable("closureReasonId");
        Long closureReasonId = null;
        if (closureReasonIdObj instanceof Integer) {
            closureReasonId = ((Integer) closureReasonIdObj).longValue();
        } else if (closureReasonIdObj instanceof Long) {
            closureReasonId = (Long) closureReasonIdObj;
        } else if (closureReasonIdObj != null) {
            closureReasonId = Long.valueOf(closureReasonIdObj.toString());
        }
        String dateFormat = (String) execution.getVariable("dateFormat");
        String locale = (String) execution.getVariable("locale");
        java.time.LocalDate closureDate = java.time.LocalDate.now();
        try {
            if (clientId == null) {
                throw new IllegalArgumentException("clientId is missing from process variables");
            }
            if (closureReasonId == null) {
                throw new IllegalArgumentException("closureReasonId is missing from process variables");
            }
            if (dateFormat == null) {
                dateFormat = "yyyy-MM-dd";
            }
            if (locale == null) {
                locale = "en";
            }
            logger.info("Closing client {} with reason ID: {}", clientId, closureReasonId);
            var closeRequest = ClientCloseRequestDTO.builder()
                    .dateFormat(dateFormat)
                    .locale(locale)
                    .closureDate(closureDate)
                    .closureReasonId(closureReasonId)
                    .build();
            fineractClientService.closeClient(clientId, "close", closeRequest).blockingFirst();
            execution.setVariable("clientClosed", true);
        } catch (org.mifos.workflow.exception.FineractApiException e) {
            logger.error("Fineract API error during client closure: {}", e.getMessage());
            execution.setVariable("clientClosed", false);
            execution.setVariable("errorMessage", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error during client closure: {}", e.getMessage(), e);
            execution.setVariable("clientClosed", false);
            execution.setVariable("errorMessage", e.getMessage());
            throw new WorkflowException("Client closure failed", e, "client closure", "ERROR_CLIENT_CLOSURE_FAILED");
        }
    }
} 