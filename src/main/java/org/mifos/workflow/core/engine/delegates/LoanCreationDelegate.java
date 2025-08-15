package org.mifos.workflow.core.engine.delegates;

import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.mifos.fineract.client.models.PostLoansResponse;
import org.mifos.workflow.exception.FineractApiException;
import org.mifos.workflow.exception.WorkflowException;
import org.mifos.workflow.service.fineract.loan.FineractLoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Delegate for creating loans in the Fineract system during workflow execution.
 * This delegate handles the loan creation process and stores the result in workflow variables.
 */
@Component
@Slf4j
public class LoanCreationDelegate implements JavaDelegate {

    private final FineractLoanService fineractLoanService;

    @Autowired
    public LoanCreationDelegate(FineractLoanService fineractLoanService) {
        this.fineractLoanService = fineractLoanService;
    }

    @Override
    public void execute(DelegateExecution execution) {
        log.info("Executing LoanCreationDelegate for process instance: {}", execution.getProcessInstanceId());

        try {
            log.info("All workflow variables: {}", execution.getVariables());

            Map<String, Object> loanRequest = buildLoanRequest(execution);

            PostLoansResponse response = fineractLoanService.createLoan(loanRequest, "submitLoanApplication");

            execution.setVariable("loanId", response.getResourceId());
            execution.setVariable("loanAccountNo", response.getResourceId());
            execution.setVariable("loanCreationSuccess", true);
            execution.setVariable("loanCreationMessage", "Loan created successfully");

            log.info("Loan created successfully with ID: {} for process instance: {}",
                    response.getResourceId(), execution.getProcessInstanceId());

        } catch (FineractApiException e) {
            log.error("Fineract API error during loan creation: {}", e.getMessage());
            execution.setVariable("loanCreationSuccess", false);
            execution.setVariable("loanCreationError", e.getMessage());
            execution.setVariable("loanCreationMessage", "Failed to create loan: " + e.getMessage());
            execution.setVariable("errorMessage", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Failed to create loan for process instance: {}", execution.getProcessInstanceId(), e);

            execution.setVariable("loanCreationSuccess", false);
            execution.setVariable("loanCreationError", e.getMessage());
            execution.setVariable("loanCreationMessage", "Failed to create loan: " + e.getMessage());
            execution.setVariable("errorMessage", e.getMessage());

            throw new WorkflowException("Loan creation failed", e, "loan creation", WorkflowException.ERROR_LOAN_CREATION_FAILED);
        }
    }

    private Map<String, Object> buildLoanRequest(DelegateExecution execution) {
        Map<String, Object> request = new HashMap<>();

        log.info("All workflow variables: {}", execution.getVariables());

        request.put("clientId", getLongVariable(execution, "clientId"));
        request.put("productId", getLongVariable(execution, "productId"));
        request.put("principal", getDoubleVariable(execution, "principal"));
        request.put("loanTermFrequency", getIntegerVariable(execution, "loanTermFrequency"));
        request.put("loanTermFrequencyType", getIntegerVariable(execution, "loanTermFrequencyType"));
        request.put("loanPurposeId", getIntegerVariable(execution, "loanPurposeId"));
        request.put("interestRatePerPeriod", getDoubleVariable(execution, "interestRatePerPeriod"));
        request.put("interestRateFrequencyType", getIntegerVariable(execution, "interestRateFrequencyType"));
        request.put("amortizationType", getIntegerVariable(execution, "amortizationType"));

        Integer interestType = getIntegerVariable(execution, "interestType");
        request.put("interestType", interestType);

        request.put("interestCalculationPeriodType", getIntegerVariable(execution, "interestCalculationPeriodType"));
        request.put("transactionProcessingStrategyCode", getStringVariable(execution, "transactionProcessingStrategyCode"));
        request.put("numberOfRepayments", getIntegerVariable(execution, "numberOfRepayments"));
        request.put("repaymentEvery", getIntegerVariable(execution, "repaymentEvery"));
        request.put("repaymentFrequencyType", getIntegerVariable(execution, "repaymentFrequencyType"));
        request.put("expectedDisbursementDate", parseDateString(getStringVariable(execution, "expectedDisbursementDate")));
        request.put("submittedOnDate", parseDateString(getStringVariable(execution, "submittedOnDate")));

        String loanType = getStringVariable(execution, "loanType");
        request.put("loanType", loanType);
        log.info("Setting loanType to: {} (type: {})", loanType, loanType.getClass().getSimpleName());

        if (execution.hasVariable("groupId") && execution.getVariable("groupId") != null) {
            request.put("groupId", getLongVariable(execution, "groupId"));
        }

        if (execution.hasVariable("externalId") && execution.getVariable("externalId") != null) {
            request.put("externalId", getStringVariable(execution, "externalId"));
        }
        if (execution.hasVariable("dateFormat") && execution.getVariable("dateFormat") != null) {
            request.put("dateFormat", getStringVariable(execution, "dateFormat"));
        } else {
            request.put("dateFormat", "yyyy-MM-dd");
        }
        if (execution.hasVariable("locale") && execution.getVariable("locale") != null) {
            request.put("locale", getStringVariable(execution, "locale"));
        } else {
            request.put("locale", "en");
        }
        if (execution.hasVariable("fundId") && execution.getVariable("fundId") != null) {
            request.put("fundId", getIntegerVariable(execution, "fundId"));
        }

        log.info("Final built loan request: {}", request);
        return request;
    }


    private Long getLongVariable(DelegateExecution execution, String variableName) {
        Object value = execution.getVariable(variableName);
        if (value == null) {
            throw new IllegalArgumentException("Required variable '" + variableName + "' is missing");
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return Long.valueOf(value.toString());
    }

    private Integer getIntegerVariable(DelegateExecution execution, String variableName) {
        return getIntegerVariable(execution, variableName, null);
    }

    private Integer getIntegerVariable(DelegateExecution execution, String variableName, Integer defaultValue) {
        Object value = execution.getVariable(variableName);
        if (value == null) {
            if (defaultValue != null) {
                return defaultValue;
            }
            throw new IllegalArgumentException("Required variable '" + variableName + "' is missing");
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return Integer.valueOf(value.toString());
    }

    private Double getDoubleVariable(DelegateExecution execution, String variableName) {
        Object value = execution.getVariable(variableName);
        if (value == null) {
            throw new IllegalArgumentException("Required variable '" + variableName + "' is missing");
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        } else if (value instanceof String) {
            String strValue = ((String) value).replace(",", "");
            try {
                return Double.valueOf(strValue);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid number format for variable '" + variableName + "': " + value);
            }
        }
        return Double.valueOf(value.toString());
    }

    private String getStringVariable(DelegateExecution execution, String variableName) {
        Object value = execution.getVariable(variableName);
        if (value == null) {
            throw new IllegalArgumentException("Required variable '" + variableName + "' is missing");
        }
        return value.toString();
    }


    private String parseDateString(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
            LocalDate date = LocalDate.parse(dateString, formatter);
            return date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
        } catch (DateTimeParseException e1) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
                LocalDate date = LocalDate.parse(dateString, formatter);
                return date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
            } catch (DateTimeParseException e2) {
                try {
                    LocalDate date = LocalDate.parse(dateString);
                    return date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
                } catch (DateTimeParseException e3) {
                    log.warn("Could not parse date string '{}', using as-is. Expected format: 'dd MMM yyyy' or 'dd MMMM yyyy' or ISO format", dateString);
                    return dateString;
                }
            }
        }
    }
}
