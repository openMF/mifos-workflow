package org.mifos.workflow.dto.fineract.loan;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * DTO for loan disbursement requests in the workflow system.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanDisbursementRequestDTO {

    @NotNull(message = "Loan ID is required")
    private Long loanId;

    private String externalId;

    @NotNull(message = "Disbursement date is required")
    private LocalDate actualDisbursementDate;

    @Positive(message = "Transaction amount must be positive")
    private BigDecimal transactionAmount;

    @NotBlank(message = "Disbursement method is required")
    private String disbursementMethod;

    private String accountNumber;

    private String note;

    private String requestNotes;

    private String dateFormat;

    private String locale;

    private List<Map<String, Object>> disbursementData;

    private Map<String, Object> additionalProperties;

    private String disbursementOfficer;
    
    private String manager;
    
    private String itSupport;
    
    private Boolean complianceCheck;
    
    private String complianceNotes;
    
    private String clientNotificationMethod;
    
    private Boolean requireClientAcknowledgement;
    
    private String escalationLevel;
    
    private Boolean autoRetryOnFailure;
    
    private Integer maxRetryAttempts;
    
    private String priority;
    
    private String riskLevel;
    
    private String disbursementChannel;
    
    private String bankCode;
    
    private String branchCode;
    
    private String referenceNumber;
    
    private String purpose;
    
    private String currencyCode;
    
    private BigDecimal exchangeRate;
    
    private String sourceOfFunds;
    
    private String destinationAccount;
    
    private String destinationBank;
    
    private String destinationBranch;
    
    private String beneficiaryName;
    
    private String beneficiaryId;
    
    private String beneficiaryPhone;
    
    private String beneficiaryEmail;
    
    private Boolean isUrgent;
    
    private String urgencyReason;
    
    private LocalDate expectedCompletionDate;
    
    private String approvalLevel;
    
    private String approverName;
    
    private LocalDate approvalDate;
    
    private String approvalNotes;
    
    private Boolean requiresManagerApproval;
    
    private Boolean requiresComplianceReview;
    
    private String complianceReviewer;
    
    private LocalDate complianceReviewDate;
    
    private String complianceReviewNotes;
    
    private Boolean requiresITSupport;
    
    private String itSupportNotes;
    
    private String systemIntegrationNotes;
    
    private String auditTrail;
    
    private String processVersion;
    
    private String workflowInstanceId;
    
    private String parentProcessId;
    
    private String correlationId;
    
    private Map<String, Object> metadata;
    
    private List<String> attachments;
    
    private String status;
    
    private String lastModifiedBy;
    
    private LocalDate lastModifiedDate;
    
    private String createdBy;
    
    private LocalDate createdDate;
}

