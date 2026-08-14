package org.mifos.workflow.dto.fineract.loan;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanCancellationRequestDTO {
    @NotNull(message = "Loan ID is required")
    private Long loanId;

    private String externalId;

    @NotBlank(message = "Cancellation reason is required")
    private String cancellationReason;

    @NotNull(message = "Cancellation date is required")
    private LocalDate cancellationDate;

    private String cancelledBy;
    private String notes;
    private String dateFormat;
    private String locale;
    
    private String loanOfficer;
    private String assignee;
    private String approver;
}
