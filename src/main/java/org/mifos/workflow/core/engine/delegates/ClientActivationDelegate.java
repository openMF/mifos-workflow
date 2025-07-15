package org.mifos.workflow.core.engine.delegates;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.mifos.fineract.client.models.PostClientsClientIdResponse;
import org.mifos.workflow.service.fineract.client.FineractClientService;
import org.mifos.workflow.exception.FineractApiException;
import org.mifos.workflow.exception.WorkflowException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import org.mifos.workflow.util.ProcessVariableUtil;

/**
 * Delegate for activating a client in the Fineract system.
 * Activates a previously created inactive client.
 */
@Component
public class ClientActivationDelegate implements JavaDelegate {
    private static final Logger logger = LoggerFactory.getLogger(ClientActivationDelegate.class);
    private static final String DEFAULT_DATE_FORMAT = "dd MMMM yyyy";
    private static final String DEFAULT_LOCALE = "en";
    
    private final FineractClientService fineractClientService;

    @Autowired
    public ClientActivationDelegate(FineractClientService fineractClientService) {
        this.fineractClientService = fineractClientService;
    }

    @Override
    public void execute(DelegateExecution execution) {
        logger.info("ClientActivationDelegate.execute() called for process instance: {}", execution.getProcessInstanceId());
        try {
            Long clientId = (Long) execution.getVariable("clientId");
            if (clientId == null) {
                throw new IllegalArgumentException("clientId is missing from process variables");
            }
            Object activationDateObj = execution.getVariable("activationDate");
            LocalDate activationDate = ProcessVariableUtil.getLocalDate(activationDateObj);
            if (activationDate == null) {
                activationDate = LocalDate.now();
            }
            logger.info("Activating client with ID: {} on date: {}", clientId, activationDate);
            PostClientsClientIdResponse response = fineractClientService.activateClient(clientId, activationDate, DEFAULT_DATE_FORMAT, DEFAULT_LOCALE).blockingFirst();
            if (response.getResourceId() != null) {
                execution.setVariable("clientActivated", true);
                execution.setVariable("clientStatus", "ACTIVE");
                execution.setVariable("activationDate", activationDate);
                logger.info("Successfully activated client with ID: {}", clientId);
            } else {
                throw new RuntimeException("Failed to activate client: No response received");
            }
        } catch (FineractApiException e) {
            logger.error("Fineract API error during client activation: {}", e.getMessage());
            execution.setVariable("clientActivated", false);
            execution.setVariable("errorMessage", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error activating client: {}", e.getMessage(), e);
            execution.setVariable("clientActivated", false);
            execution.setVariable("errorMessage", e.getMessage());
            throw new WorkflowException("Client activation failed", e, "client activation", WorkflowException.ERROR_CLIENT_ACTIVATION_FAILED);
        }
    }
} 