package org.mifos.workflow.core.engine.delegates;

import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.mifos.fineract.client.models.DeleteLoansLoanIdResponse;
import org.mifos.workflow.exception.FineractApiException;
import org.mifos.workflow.exception.WorkflowException;
import org.mifos.workflow.service.fineract.loan.FineractLoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Delegate for cancelling/deleting loan applications in Fineract.
 * This delegate is executed as part of the loan cancellation workflow.
 */
@Component
@Slf4j
public class LoanCancellationDelegate implements JavaDelegate {

    private final FineractLoanService fineractLoanService;

    @Autowired
    public LoanCancellationDelegate(FineractLoanService fineractLoanService) {
        this.fineractLoanService = fineractLoanService;
    }

    @Override
    public void execute(DelegateExecution execution) {
        log.info("Executing LoanCancellationDelegate for process instance: {}", execution.getProcessInstanceId());

        try {
            Object loanIdObj = execution.getVariable("loanId");
            Long loanId = null;

            if (loanIdObj instanceof Integer) {
                loanId = ((Integer) loanIdObj).longValue();
            } else if (loanIdObj instanceof Long) {
                loanId = (Long) loanIdObj;
            } else if (loanIdObj instanceof String) {
                loanId = Long.parseLong((String) loanIdObj);
            } else if (loanIdObj != null) {
                loanId = Long.valueOf(loanIdObj.toString());
            }

            DeleteLoansLoanIdResponse response = fineractLoanService.deleteLoan(loanId);

            execution.setVariable("cancellationSuccessful", true);
            execution.setVariable("cancelledLoanId", loanId);
            execution.setVariable("cancelledResourceId", response.getResourceId());
            execution.setVariable("cancelledClientId", response.getClientId());
            execution.setVariable("cancelledOfficeId", response.getOfficeId());
            execution.setVariable("cancellationTimestamp", System.currentTimeMillis());

            log.info("Loan cancellation completed successfully. Loan ID: {}, Response ID: {}",
                    loanId, response.getResourceId());

        } catch (FineractApiException e) {
            log.error("Fineract API error during loan cancellation: {}", e.getMessage());
            execution.setVariable("cancellationSuccessful", false);
            execution.setVariable("cancellationError", e.getMessage());
            execution.setVariable("errorMessage", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error during loan cancellation for process instance: {}",
                    execution.getProcessInstanceId(), e);
            execution.setVariable("cancellationSuccessful", false);
            execution.setVariable("cancellationError", e.getMessage());
            execution.setVariable("errorMessage", e.getMessage());
            throw new WorkflowException("Loan cancellation failed", e, "loan cancellation", WorkflowException.ERROR_LOAN_CANCELLATION_FAILED);
        }
    }
}
