package org.mifos.workflow.core.engine.delegates;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Delegate for rejecting a client in the Fineract system.
 * This is a placeholder implementation.
 * Full implementation will be provided later.
 */
public class ClientRejectionDelegate implements JavaDelegate {

    private static final Logger logger = LoggerFactory.getLogger(ClientRejectionDelegate.class);

    @Override
    public void execute(DelegateExecution execution) {
        // TODO: Implement client rejection logic
        logger.info("ClientRejectionDelegate.execute() called - placeholder implementation");
        logger.info("Process instance ID: {}", execution.getProcessInstanceId());
        logger.info("Variables: {}", execution.getVariables());

        Long clientId = (Long) execution.getVariable("clientId");
        if (clientId == null) {
            throw new IllegalArgumentException("clientId is missing from process variables");
        }
        String clientIdStr = clientId.toString();
        String rejectionReason = (String) execution.getVariable("rejectionReason");
        logger.info("Would reject client with ID: {} for reason: {}", clientIdStr, rejectionReason);
    }
} 