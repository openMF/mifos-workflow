package org.mifos.workflow.core.engine.delegates;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.mifos.workflow.exception.WorkflowException;
import org.mifos.workflow.service.fineract.client.FineractClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.mifos.fineract.client.models.GetClientsClientIdAccountsResponse;


/**
 * Delegate for verifying client accounts in the Fineract system.
 */
@Component
public class AccountVerificationDelegate implements JavaDelegate {

    private static final Logger logger = LoggerFactory.getLogger(AccountVerificationDelegate.class);
    private final FineractClientService fineractClientService;

    @Autowired
    public AccountVerificationDelegate(FineractClientService fineractClientService) {
        this.fineractClientService = fineractClientService;
    }

    @Override
    public void execute(DelegateExecution execution) {
        Long clientId = (Long) execution.getVariable("clientId");
        try {
            if (clientId == null) {
                throw new IllegalArgumentException("clientId is missing from process variables");
            }
            logger.info("Verifying accounts for client {}", clientId);
            GetClientsClientIdAccountsResponse response = fineractClientService.retrieveClientAccounts(clientId).blockingFirst();
            boolean hasActiveLoans = response.getLoanAccounts() != null &&
                    response.getLoanAccounts().stream().anyMatch(loan ->
                            loan.getStatus() != null && "active".equalsIgnoreCase(loan.getStatus().getCode())
                    );
            execution.setVariable("hasActiveLoans", hasActiveLoans);
            execution.setVariable("accountsVerified", true);
        } catch (org.mifos.workflow.exception.FineractApiException e) {
            logger.error("Fineract API error during account verification: {}", e.getMessage());
            execution.setVariable("accountsVerified", false);
            execution.setVariable("errorMessage", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error during account verification: {}", e.getMessage(), e);
            execution.setVariable("accountsVerified", false);
            execution.setVariable("errorMessage", e.getMessage());
            throw new WorkflowException("Account verification failed", e, "account verification", "ERROR_ACCOUNT_VERIFICATION_FAILED");
        }
    }
} 