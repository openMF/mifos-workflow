package org.mifos.workflow.core.engine.delegates;

import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.mifos.fineract.client.models.PostLoansLoanIdResponse;
import org.mifos.workflow.exception.FineractApiException;
import org.mifos.workflow.exception.WorkflowException;
import org.mifos.workflow.service.fineract.loan.FineractLoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Delegate for disbursing loans in the Fineract system during workflow execution.
 */
@Component
@Slf4j
public class LoanDisbursementDelegate implements JavaDelegate {

    private final FineractLoanService fineractLoanService;

    @Autowired
    public LoanDisbursementDelegate(FineractLoanService fineractLoanService) {
        this.fineractLoanService = fineractLoanService;
    }

    @Override
    public void execute(DelegateExecution execution) {
        log.info("Executing LoanDisbursementDelegate for process instance: {}", execution.getProcessInstanceId());

        try {
            int retryAttempt = getRetryAttempt(execution);
            int maxRetryAttempts = getMaxRetryAttempts(execution);
            log.info("Disbursement attempt {} of {} for process instance: {}", retryAttempt, maxRetryAttempts, execution.getProcessInstanceId());

            Map<String, Object> disbursementRequest = buildDisbursementRequest(execution);

            Long loanId = (Long) execution.getVariable("loanId");
            if (loanId == null) {
                throw new IllegalArgumentException("Loan ID is required for disbursement");
            }

            Object disbursementDate = execution.getVariable("actualDisbursementDate");
            if (disbursementDate == null) {
                throw new IllegalArgumentException("Disbursement date is required");
            }

            log.info("Attempting to disburse loan {} with amount: {}, date: {}", loanId, disbursementRequest.get("transactionAmount"), disbursementRequest.get("actualDisbursementDate"));

            PostLoansLoanIdResponse response = fineractLoanService.performStateTransition(loanId, disbursementRequest, "disburse");

            execution.setVariable("loanDisbursementSuccess", true);
            execution.setVariable("loanDisbursementMessage", "Loan disbursed successfully");
            execution.setVariable("loanStatus", "DISBURSED");
            execution.setVariable("disbursementTransactionId", response.getResourceId());
            execution.setVariable("disbursementCompletedDate", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            execution.setVariable("disbursementCompletedBy", execution.getVariable("disbursementOfficer"));

            execution.setVariable("actualDisbursementAmount", disbursementRequest.get("transactionAmount"));

            execution.setVariable("retryAttempt", 0);
            execution.setVariable("lastError", null);
            execution.setVariable("escalated", false);

            log.info("Loan disbursed successfully with ID: {} for process instance: {}", loanId, execution.getProcessInstanceId());

        } catch (FineractApiException e) {
            log.error("Fineract API error during loan disbursement: {}", e.getMessage());
            handleDisbursementFailure(execution, e, "Fineract API Error");
            throw e;
        } catch (Exception e) {
            log.error("Failed to disburse loan for process instance: {}", execution.getProcessInstanceId(), e);
            handleDisbursementFailure(execution, e, "System Error");
            throw new WorkflowException("Loan disbursement failed", e, "loan disbursement", WorkflowException.ERROR_LOAN_DISBURSEMENT_FAILED);
        }
    }

    private void handleDisbursementFailure(DelegateExecution execution, Exception e, String errorType) {
        int retryAttempt = getRetryAttempt(execution);
        int maxRetryAttempts = getMaxRetryAttempts(execution);
        boolean autoRetry = getAutoRetryOnFailure(execution);

        execution.setVariable("loanDisbursementSuccess", false);
        execution.setVariable("loanDisbursementError", e.getMessage());
        execution.setVariable("loanDisbursementMessage", "Failed to disburse loan: " + e.getMessage());
        execution.setVariable("errorMessage", e.getMessage());
        execution.setVariable("errorType", errorType);
        execution.setVariable("lastError", e.getMessage());
        execution.setVariable("lastErrorDate", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        retryAttempt++;
        execution.setVariable("retryAttempt", retryAttempt);

        if (autoRetry && retryAttempt < maxRetryAttempts) {
            execution.setVariable("shouldRetry", true);
            execution.setVariable("retryReason", "Automatic retry after failure");
            log.info("Scheduling automatic retry {} of {} for process instance: {}", retryAttempt, maxRetryAttempts, execution.getProcessInstanceId());
        } else {
            execution.setVariable("shouldRetry", false);
            execution.setVariable("maxRetriesExceeded", retryAttempt >= maxRetryAttempts);
            execution.setVariable("escalationRequired", true);
        }

        execution.setVariable("failureReason", e.getMessage());
        execution.setVariable("failureType", errorType);
        execution.setVariable("failureTimestamp", System.currentTimeMillis());
    }

    private Map<String, Object> buildDisbursementRequest(DelegateExecution execution) {
        Map<String, Object> request = new HashMap<>();

        Object disbursementDate = execution.getVariable("actualDisbursementDate");
        String dateFormat = execution.hasVariable("dateFormat") && execution.getVariable("dateFormat") != null ? execution.getVariable("dateFormat").toString() : "yyyy-MM-dd";

        String disbursementDateStr = formatDateVariable(disbursementDate, dateFormat);
        if (disbursementDateStr == null || disbursementDateStr.isEmpty()) {
            disbursementDateStr = LocalDate.now().format(DateTimeFormatter.ofPattern(dateFormat));
        }
        request.put("actualDisbursementDate", disbursementDateStr);

        Object approvedAmt = execution.getVariable("approvedAmount");
        Object originalAmt = execution.getVariable("transactionAmount");
        Object amt = approvedAmt != null ? approvedAmt : originalAmt;
        if (amt != null) {
            request.put("transactionAmount", amt);
            log.info("Using amount for disbursement: {} (approved: {}, original: {})", amt, approvedAmt, originalAmt);
        }
        Object note = execution.getVariable("note");
        if (note != null) {
            request.put("note", note);
        }
        if (execution.hasVariable("disbursementData") && execution.getVariable("disbursementData") != null) {
            request.put("disbursementData", execution.getVariable("disbursementData"));
        }

        request.put("dateFormat", dateFormat);
        request.put("locale", execution.hasVariable("locale") && execution.getVariable("locale") != null ? execution.getVariable("locale").toString() : "en");

        log.info("Built disbursement request (sanitized): {}", request);
        return request;
    }

    private String formatDateVariable(Object dateVar, String dateFormat) {
        if (dateVar == null) {
            return null;
        }
        try {
            if (dateVar instanceof String) {
                return (String) dateVar;
            }
            if (dateVar instanceof LocalDate) {
                return ((LocalDate) dateVar).format(DateTimeFormatter.ofPattern(dateFormat));
            }
            return dateVar.toString();
        } catch (Exception e) {
            log.warn("Could not format disbursement date variable '{}' with format '{}': {}", dateVar, dateFormat, e.getMessage());
            return null;
        }
    }

    private int getRetryAttempt(DelegateExecution execution) {
        Object retryAttempt = execution.getVariable("retryAttempt");
        return retryAttempt != null ? (Integer) retryAttempt : 0;
    }

    private int getMaxRetryAttempts(DelegateExecution execution) {
        Object maxRetryAttempts = execution.getVariable("maxRetryAttempts");
        return maxRetryAttempts != null ? (Integer) maxRetryAttempts : 3;
    }

    private boolean getAutoRetryOnFailure(DelegateExecution execution) {
        Object autoRetry = execution.getVariable("autoRetryOnFailure");
        return autoRetry != null ? (Boolean) autoRetry : true;
    }
}

