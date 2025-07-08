package org.mifos.workflow.core.engine.delegates;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Delegate for rejecting a client transfer in the Fineract system.
 * This is a placeholder implementation.
 * Full implementation will be provided later.
 */
public class TransferRejectionDelegate implements JavaDelegate {

    private static final Logger logger = LoggerFactory.getLogger(TransferRejectionDelegate.class);

    @Override
    public void execute(DelegateExecution execution) {
        // TODO: Implement transfer rejection logic
        logger.info("TransferRejectionDelegate.execute() called - placeholder implementation");
        logger.info("Process instance ID: {}", execution.getProcessInstanceId());
        logger.info("Variables: {}", execution.getVariables());

        Long clientId = (Long) execution.getVariable("clientId");
        if (clientId == null) {
            // handle missing clientId (log, throw, etc.)
            throw new IllegalArgumentException("clientId is missing from process variables");
        }
        String rejectionReason = (String) execution.getVariable("rejectionReason");
        logger.info("Would reject transfer for client {} with reason: {}", clientId, rejectionReason);
    }
} 