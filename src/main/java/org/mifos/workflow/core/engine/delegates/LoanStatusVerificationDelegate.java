package org.mifos.workflow.core.engine.delegates;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.mifos.fineract.client.models.GetLoansLoanIdResponse;
import org.mifos.workflow.exception.FineractApiException;
import org.mifos.workflow.exception.WorkflowException;
import org.mifos.workflow.service.fineract.loan.FineractLoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


/**
 * Delegate for verifying loan status in the Fineract system during workflow execution.
 * This delegate checks if a loan is approved and ready for disbursement.
 */
@Component
@Slf4j
public class LoanStatusVerificationDelegate implements JavaDelegate {

    private final FineractLoanService fineractLoanService;

    @Autowired
    public LoanStatusVerificationDelegate(FineractLoanService fineractLoanService) {
        this.fineractLoanService = fineractLoanService;
    }

    @Override
    public void execute(DelegateExecution execution) {
        log.info("Executing LoanStatusVerificationDelegate for process instance: {}", execution.getProcessInstanceId());

        try {
            Long loanId = (Long) execution.getVariable("loanId");

            if (loanId == null) {
                throw new IllegalArgumentException("Loan ID is required for status verification");
            }

            log.info("Verifying loan status for loan ID: {}", loanId);

            String associations = determineAssociations(execution);
            String fields = determineFields(execution);

            GetLoansLoanIdResponse loanResponse;
            if (associations != null || fields != null) {
                loanResponse = fineractLoanService.getLoan(loanId, null, associations, null, fields);
            } else {
                loanResponse = fineractLoanService.getLoan(loanId, null, null, null, null);
            }

            if (loanResponse == null) {
                throw new RuntimeException("Could not retrieve loan from Fineract");
            }

            LoanVerificationResult verificationResult = performComprehensiveVerification(loanResponse, execution);

            execution.setVariable("loanStatus", verificationResult.getLoanStatus());
            execution.setVariable("loanStatusVerified", true);
            execution.setVariable("loanReadyForDisbursement", verificationResult.isReadyForDisbursement());
            execution.setVariable("loanStatusMessage", verificationResult.getStatusMessage());
            execution.setVariable("verificationDate", LocalDate.now().toString());
            execution.setVariable("verificationPerformedBy", execution.getVariable("disbursementOfficer"));

            execution.setVariable("loanAccountNo", verificationResult.getAccountNo());
            execution.setVariable("loanPrincipal", verificationResult.getPrincipal());
            execution.setVariable("loanOutstandingBalance", verificationResult.getOutstandingBalance());
            execution.setVariable("loanProductId", verificationResult.getProductId());
            execution.setVariable("loanClientId", verificationResult.getClientId());
            execution.setVariable("loanOfficeId", verificationResult.getOfficeId());
            execution.setVariable("loanCurrencyCode", verificationResult.getCurrencyCode());
            execution.setVariable("loanTermFrequency", verificationResult.getTermFrequency());
            execution.setVariable("loanInterestRate", verificationResult.getInterestRate());

            if (!verificationResult.getIssues().isEmpty()) {
                execution.setVariable("verificationIssues", verificationResult.getIssues());
                execution.setVariable("hasVerificationIssues", true);
                execution.setVariable("issueCount", verificationResult.getIssues().size());
            } else {
                execution.setVariable("hasVerificationIssues", false);
                execution.setVariable("issueCount", 0);
            }

            execution.setVariable("complianceCheckRequired", verificationResult.isComplianceCheckRequired());
            execution.setVariable("riskLevel", verificationResult.getRiskLevel());
            execution.setVariable("approvalLevel", verificationResult.getApprovalLevel());

            if (!verificationResult.isReadyForDisbursement()) {
                execution.setVariable("loanStatusError", verificationResult.getStatusMessage());
                execution.setVariable("blockingIssues", verificationResult.getBlockingIssues());
                execution.setVariable("escalationRequired", verificationResult.isEscalationRequired());
            }

            log.info("Loan status verification completed for ID: {} - Status: {}, Ready for disbursement: {}, Issues: {}",
                    loanId, verificationResult.getLoanStatus(), verificationResult.isReadyForDisbursement(),
                    verificationResult.getIssues().size());

        } catch (FineractApiException e) {
            log.error("Fineract API error during loan status verification: {}", e.getMessage());
            handleVerificationFailure(execution, e, "Fineract API Error");
            throw e;
        } catch (Exception e) {
            log.error("Failed to verify loan status for process instance: {}", execution.getProcessInstanceId(), e);
            handleVerificationFailure(execution, e, "System Error");
            throw new WorkflowException("Loan status verification failed", e, "loan status verification", WorkflowException.ERROR_LOAN_STATUS_VERIFICATION_FAILED);
        }
    }

    private String determineAssociations(DelegateExecution execution) {
        return null;
    }

    private String determineFields(DelegateExecution execution) {
        return "id,accountNo,status,principal,loanProductId,clientId,currency";
    }

    private LoanVerificationResult performComprehensiveVerification(GetLoansLoanIdResponse loanResponse, DelegateExecution execution) {
        LoanVerificationResult result = new LoanVerificationResult();

        result.setLoanStatus(loanResponse.getStatus().toString());
        result.setAccountNo(loanResponse.getAccountNo());
        result.setPrincipal(loanResponse.getPrincipal() != null ? new BigDecimal(loanResponse.getPrincipal().toString()) : null);
        result.setOutstandingBalance(loanResponse.getSummary() != null && loanResponse.getSummary().getTotalOutstanding() != null ?
                new BigDecimal(loanResponse.getSummary().getTotalOutstanding().toString()) : null);
        result.setProductId(loanResponse.getLoanProductId());
        result.setClientId(loanResponse.getClientId());
        result.setOfficeId(null);
        result.setCurrencyCode(loanResponse.getCurrency() != null ? loanResponse.getCurrency().getCode() : null);
        result.setTermFrequency(loanResponse.getTermFrequency());
        result.setInterestRate(loanResponse.getInterestRatePerPeriod() != null ? new BigDecimal(loanResponse.getInterestRatePerPeriod().toString()) : null);

        boolean isApproved = "APPROVED".equalsIgnoreCase(result.getLoanStatus());
        result.setApproved(isApproved);

        boolean isDisbursed = "DISBURSED".equalsIgnoreCase(result.getLoanStatus());
        result.setDisbursed(isDisbursed);

        boolean isActive = loanResponse.getStatus().getActive() != null ? loanResponse.getStatus().getActive() : false;
        result.setActive(isActive);

        boolean isPendingApproval = "PENDING_APPROVAL".equalsIgnoreCase(result.getLoanStatus());
        result.setPendingApproval(isPendingApproval);

        boolean isRejected = "REJECTED".equalsIgnoreCase(result.getLoanStatus());
        result.setRejected(isRejected);

        boolean isWithdrawn = "WITHDRAWN".equalsIgnoreCase(result.getLoanStatus());
        result.setWithdrawn(isWithdrawn);

        performAdditionalChecks(loanResponse, result, execution);

        boolean readyForDisbursement = isApproved && !isDisbursed && isActive && !isRejected && !isWithdrawn;
        result.setReadyForDisbursement(readyForDisbursement);

        if (readyForDisbursement) {
            result.setStatusMessage("Loan is approved and ready for disbursement");
        } else if (isDisbursed) {
            result.setStatusMessage("Loan is already disbursed");
            result.addIssue("Loan has already been disbursed");
        } else if (!isApproved) {
            result.setStatusMessage("Loan is not approved. Current status: " + result.getLoanStatus());
            result.addIssue("Loan is not in approved status");
        } else if (isRejected) {
            result.setStatusMessage("Loan has been rejected");
            result.addIssue("Loan has been rejected and cannot be disbursed");
        } else if (isWithdrawn) {
            result.setStatusMessage("Loan has been withdrawn");
            result.addIssue("Loan has been withdrawn and cannot be disbursed");
        } else {
            result.setStatusMessage("Loan is not ready for disbursement. Status: " + result.getLoanStatus());
            result.addIssue("Loan status is not suitable for disbursement");
        }

        return result;
    }

    private void performAdditionalChecks(GetLoansLoanIdResponse loanResponse, LoanVerificationResult result, DelegateExecution execution) {
        BigDecimal requestedAmount = (BigDecimal) execution.getVariable("transactionAmount");
        if (requestedAmount != null && loanResponse.getPrincipal() != null) {
            BigDecimal principal = new BigDecimal(loanResponse.getPrincipal().toString());
            if (requestedAmount.compareTo(principal) > 0) {
                result.addIssue("Requested disbursement amount (" + requestedAmount + ") exceeds approved principal (" + principal + ")");
            }
        }

        if (loanResponse.getCharges() != null && !loanResponse.getCharges().isEmpty()) {
            result.addIssue("Loan has pending charges that need to be resolved");
        }

        if (loanResponse.getSummary() != null && loanResponse.getSummary().getTotalOverdue() != null) {
            BigDecimal totalOverdue = new BigDecimal(loanResponse.getSummary().getTotalOverdue().toString());
            if (totalOverdue.compareTo(BigDecimal.ZERO) > 0) {
                result.addIssue("Loan has overdue payments");
            }
        }

        if (loanResponse.getTermFrequency() != null && loanResponse.getTermFrequency() <= 0) {
            result.addIssue("Loan term frequency is invalid");
        }

        if (loanResponse.getInterestRatePerPeriod() != null) {
            BigDecimal interestRate = new BigDecimal(loanResponse.getInterestRatePerPeriod().toString());
            if (interestRate.compareTo(BigDecimal.ZERO) < 0) {
                result.addIssue("Loan interest rate is negative");
            }
        }

        String riskLevel = determineRiskLevel(loanResponse);
        result.setRiskLevel(riskLevel);

        boolean complianceRequired = "HIGH".equals(riskLevel) || requestedAmount != null && requestedAmount.compareTo(new BigDecimal("1000000")) > 0;
        result.setComplianceCheckRequired(complianceRequired);

        String approvalLevel = determineApprovalLevel(loanResponse, requestedAmount);
        result.setApprovalLevel(approvalLevel);

        boolean escalationRequired = "HIGH".equals(riskLevel) || "SENIOR_MANAGER".equals(approvalLevel);
        result.setEscalationRequired(escalationRequired);
    }

    private String determineRiskLevel(GetLoansLoanIdResponse loanResponse) {
        if (loanResponse.getPrincipal() != null) {
            BigDecimal principal = new BigDecimal(loanResponse.getPrincipal().toString());
            if (principal.compareTo(new BigDecimal("500000")) > 0) {
                return "HIGH";
            } else if (principal.compareTo(new BigDecimal("100000")) > 0) {
                return "MEDIUM";
            }
        }
        return "LOW";
    }

    private String determineApprovalLevel(GetLoansLoanIdResponse loanResponse, BigDecimal requestedAmount) {
        if (requestedAmount != null) {
            if (requestedAmount.compareTo(new BigDecimal("1000000")) > 0) {
                return "SENIOR_MANAGER";
            } else if (requestedAmount.compareTo(new BigDecimal("500000")) > 0) {
                return "MANAGER";
            }
        }
        return "OFFICER";
    }

    private void handleVerificationFailure(DelegateExecution execution, Exception e, String errorType) {
        execution.setVariable("loanStatusVerified", false);
        execution.setVariable("loanStatusError", e.getMessage());
        execution.setVariable("loanStatusMessage", "Failed to verify loan status: " + e.getMessage());
        execution.setVariable("loanReadyForDisbursement", false);
        execution.setVariable("errorMessage", e.getMessage());
        execution.setVariable("errorType", errorType);
        execution.setVariable("verificationFailed", true);
        execution.setVariable("verificationFailureDate", LocalDate.now().toString());
        execution.setVariable("verificationFailureReason", e.getMessage());
    }

    private static class LoanVerificationResult {
        @Setter
        private String loanStatus;
        @Setter
        private String accountNo;
        @Setter
        private BigDecimal principal;
        @Setter
        private BigDecimal outstandingBalance;
        @Setter
        private Long productId;
        @Setter
        private Long clientId;
        @Setter
        private Long officeId;
        @Setter
        private String currencyCode;
        @Setter
        private Integer termFrequency;
        @Setter
        private BigDecimal interestRate;
        @Setter
        private boolean approved;
        @Setter
        private boolean disbursed;
        @Setter
        private boolean active;
        @Setter
        private boolean pendingApproval;
        @Setter
        private boolean rejected;
        @Setter
        private boolean withdrawn;
        @Setter
        private boolean readyForDisbursement;
        @Setter
        private String statusMessage;
        private List<String> issues = new java.util.ArrayList<>();
        private List<String> blockingIssues = new java.util.ArrayList<>();
        @Setter
        private String riskLevel;
        @Setter
        private boolean complianceCheckRequired;
        @Setter
        private String approvalLevel;
        @Setter
        private boolean escalationRequired;

        public String getLoanStatus() {
            return loanStatus;
        }

        public String getAccountNo() {
            return accountNo;
        }

        public BigDecimal getPrincipal() {
            return principal;
        }

        public BigDecimal getOutstandingBalance() {
            return outstandingBalance;
        }

        public Long getProductId() {
            return productId;
        }

        public Long getClientId() {
            return clientId;
        }

        public Long getOfficeId() {
            return officeId;
        }

        public String getCurrencyCode() {
            return currencyCode;
        }

        public Integer getTermFrequency() {
            return termFrequency;
        }

        public BigDecimal getInterestRate() {
            return interestRate;
        }

        public boolean isApproved() {
            return approved;
        }

        public boolean isDisbursed() {
            return disbursed;
        }

        public boolean isActive() {
            return active;
        }

        public boolean isPendingApproval() {
            return pendingApproval;
        }

        public boolean isRejected() {
            return rejected;
        }

        public boolean isWithdrawn() {
            return withdrawn;
        }

        public boolean isReadyForDisbursement() {
            return readyForDisbursement;
        }

        public String getStatusMessage() {
            return statusMessage;
        }

        public List<String> getIssues() {
            return issues;
        }

        public void addIssue(String issue) {
            this.issues.add(issue);
            if (!this.readyForDisbursement) {
                this.blockingIssues.add(issue);
            }
        }

        public List<String> getBlockingIssues() {
            return blockingIssues;
        }

        public String getRiskLevel() {
            return riskLevel;
        }

        public boolean isComplianceCheckRequired() {
            return complianceCheckRequired;
        }

        public String getApprovalLevel() {
            return approvalLevel;
        }

        public boolean isEscalationRequired() {
            return escalationRequired;
        }
    }
}
