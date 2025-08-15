package org.mifos.workflow.dto.fineract.loan;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.Map;

/**
 * DTO for loan approval requests in the workflow system.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanApprovalRequestDTO {

    @NotNull(message = "Loan ID is required")
    private Long loanId;

    private String externalId;

    @NotNull(message = "Approval date is required")
    private LocalDate approvedOnDate;

    private String approvedByUsername;

    private String note;

    private String dateFormat;

    private String locale;

    private Map<String, Object> additionalProperties;
}

