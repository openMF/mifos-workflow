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
 * Delegate for approving loans in the Fineract system during workflow execution.
 * This delegate handles the loan approval process and stores the result in workflow variables.
 */
@Component
@Slf4j
public class LoanApprovalDelegate implements JavaDelegate {

    private final FineractLoanService fineractLoanService;

    @Autowired
    public LoanApprovalDelegate(FineractLoanService fineractLoanService) {
        this.fineractLoanService = fineractLoanService;
    }

    @Override
    public void execute(DelegateExecution execution) {
        log.info("Executing LoanApprovalDelegate for process instance: {}", execution.getProcessInstanceId());

        try {
            Map<String, Object> approvalRequest = buildApprovalRequest(execution);

            Long loanId = (Long) execution.getVariable("loanId");

            PostLoansLoanIdResponse response = fineractLoanService.performStateTransition(loanId, approvalRequest, "approve");

            execution.setVariable("loanApprovalSuccess", true);
            execution.setVariable("loanApprovalMessage", "Loan approved successfully");
            execution.setVariable("loanStatus", "APPROVED");

            log.info("Loan approved successfully with ID: {} for process instance: {}", loanId, execution.getProcessInstanceId());

        } catch (FineractApiException e) {
            log.error("Fineract API error during loan approval: {}", e.getMessage());
            execution.setVariable("loanApprovalSuccess", false);
            execution.setVariable("loanApprovalError", e.getMessage());
            execution.setVariable("loanApprovalMessage", "Failed to approve loan: " + e.getMessage());
            execution.setVariable("errorMessage", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Failed to approve loan for process instance: {}", execution.getProcessInstanceId(), e);

            execution.setVariable("loanApprovalSuccess", false);
            execution.setVariable("loanApprovalError", e.getMessage());
            execution.setVariable("loanApprovalMessage", "Failed to approve loan: " + e.getMessage());
            execution.setVariable("errorMessage", e.getMessage());

            throw new WorkflowException("Loan approval failed", e, "loan approval", WorkflowException.ERROR_LOAN_APPROVAL_FAILED);
        }
    }

    private Map<String, Object> buildApprovalRequest(DelegateExecution execution) {
        Map<String, Object> request = new HashMap<>();

        Object approvedOnDateVar = execution.getVariable("approvedOnDate");
        String dateFormat = execution.hasVariable("dateFormat") && execution.getVariable("dateFormat") != null ? execution.getVariable("dateFormat").toString() : "yyyy-MM-dd";
        String locale = execution.hasVariable("locale") && execution.getVariable("locale") != null ? execution.getVariable("locale").toString() : "en";

        String approvedOnDateStr = formatDateVariable(approvedOnDateVar, dateFormat);
        if (approvedOnDateStr == null || approvedOnDateStr.isEmpty()) {
            approvedOnDateStr = LocalDate.now().format(DateTimeFormatter.ofPattern(dateFormat));
        }
        request.put("approvedOnDate", approvedOnDateStr);

        if (execution.hasVariable("approvedByUsername")) {
            request.put("approvedByUsername", execution.getVariable("approvedByUsername"));
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
            log.warn("Could not format approvedOnDate variable '{}' with format '{}': {}", dateVar, dateFormat, e.getMessage());
            return null;
        }
    }
}

