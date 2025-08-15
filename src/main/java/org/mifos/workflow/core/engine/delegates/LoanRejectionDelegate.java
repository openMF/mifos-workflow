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
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Delegate for rejecting loans in the Fineract system during workflow execution.
 * This delegate handles the loan rejection process and stores the result in workflow variables.
 */
@Component
@Slf4j
public class LoanRejectionDelegate implements JavaDelegate {

    private final FineractLoanService fineractLoanService;

    @Autowired
    public LoanRejectionDelegate(FineractLoanService fineractLoanService) {
        this.fineractLoanService = fineractLoanService;
    }

    @Override
    public void execute(DelegateExecution execution) {
        log.info("Executing LoanRejectionDelegate for process instance: {}", execution.getProcessInstanceId());

        try {
            Map<String, Object> rejectionRequest = buildRejectionRequest(execution);

            Long loanId = (Long) execution.getVariable("loanId");

            PostLoansLoanIdResponse response = fineractLoanService.performStateTransition(loanId, rejectionRequest, "reject");

            execution.setVariable("loanRejectionSuccess", true);
            execution.setVariable("loanRejectionMessage", "Loan rejected successfully");
            execution.setVariable("loanStatus", "REJECTED");

            log.info("Loan rejected successfully with ID: {} for process instance: {}", loanId, execution.getProcessInstanceId());

        } catch (FineractApiException e) {
            log.error("Fineract API error during loan rejection: {}", e.getMessage());
            execution.setVariable("loanRejectionSuccess", false);
            execution.setVariable("loanRejectionError", e.getMessage());
            execution.setVariable("loanRejectionMessage", "Failed to reject loan: " + e.getMessage());
            execution.setVariable("errorMessage", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Failed to reject loan for process instance: {}", execution.getProcessInstanceId(), e);

            execution.setVariable("loanRejectionSuccess", false);
            execution.setVariable("loanRejectionError", e.getMessage());
            execution.setVariable("loanRejectionMessage", "Failed to reject loan: " + e.getMessage());
            execution.setVariable("errorMessage", e.getMessage());

            throw new WorkflowException("Loan rejection failed", e, "loan rejection", WorkflowException.ERROR_LOAN_REJECTION_FAILED);
        }
    }

    private Map<String, Object> buildRejectionRequest(DelegateExecution execution) {
        Map<String, Object> request = new HashMap<>();

        Object rejectedOnDateVar = execution.getVariable("rejectedOnDate");
        String dateFormat = execution.hasVariable("dateFormat") && execution.getVariable("dateFormat") != null ? execution.getVariable("dateFormat").toString() : "yyyy-MM-dd";
        String locale = execution.hasVariable("locale") && execution.getVariable("locale") != null ? execution.getVariable("locale").toString() : "en";

        String rejectedOnDateStr = formatDateVariable(rejectedOnDateVar, dateFormat);
        if (rejectedOnDateStr == null || rejectedOnDateStr.isEmpty()) {
            rejectedOnDateStr = LocalDate.now().format(DateTimeFormatter.ofPattern(dateFormat));
        }
        request.put("rejectedOnDate", rejectedOnDateStr);


        if (execution.hasVariable("rejectedByUsername")) {
            request.put("rejectedByUsername", execution.getVariable("rejectedByUsername"));
        }
        if (execution.hasVariable("note")) {
            request.put("note", execution.getVariable("note"));
        }
        request.put("dateFormat", dateFormat);
        request.put("locale", locale);

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
            if (dateVar instanceof Date) {
                LocalDate ld = ((Date) dateVar).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                return ld.format(DateTimeFormatter.ofPattern(dateFormat));
            }
            return dateVar.toString();
        } catch (Exception e) {
            log.warn("Could not format rejectedOnDate variable '{}' with format '{}': {}", dateVar, dateFormat, e.getMessage());
            return null;
        }
    }
}

