package org.mifos.workflow.dto.fineract.loan;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * DTO for loan creation requests in the workflow system.
 * Maps to the loan creation parameters used in Fineract API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanCreateRequestDTO {

    @NotNull(message = "Client ID is required")
    private Long clientId;

    private Long groupId;

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Principal amount is required")
    @Positive(message = "Principal amount must be positive")
    private BigDecimal principal;


    public void setPrincipal(Object principal) {
        if (principal instanceof BigDecimal) {
            this.principal = (BigDecimal) principal;
        } else if (principal instanceof String) {
            String principalStr = ((String) principal).replace(",", "");
            try {
                this.principal = new BigDecimal(principalStr);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid principal amount: " + principal + ". Must be a valid number.");
            }
        } else if (principal instanceof Number) {
            this.principal = new BigDecimal(principal.toString());
        } else {
            throw new IllegalArgumentException("Invalid principal type: " + (principal != null ? principal.getClass().getSimpleName() : "null"));
        }

        if (this.principal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Principal amount must be positive, got: " + this.principal);
        }
    }

    @NotNull(message = "Loan term frequency is required")
    private Integer loanTermFrequency;

    @NotNull(message = "Loan term frequency type is required")
    private Integer loanTermFrequencyType;

    @NotNull(message = "Loan type is required")
    private String loanType;

    @NotNull(message = "Loan purpose ID is required")
    private Long loanPurposeId;

    @NotNull(message = "Interest rate per period is required")
    private BigDecimal interestRatePerPeriod;

    @NotNull(message = "Interest rate frequency type is required")
    private Integer interestRateFrequencyType;

    @NotNull(message = "Amortization type is required")
    private Integer amortizationType;

    @NotNull(message = "Interest type is required")
    private Integer interestType;

    @NotNull(message = "Interest calculation period type is required")
    private Integer interestCalculationPeriodType;

    @NotNull(message = "Transaction processing strategy ID is required")
    private Long transactionProcessingStrategyId;

    @NotNull(message = "Loan date is required")
    private LocalDate loanDate;


    private LocalDate submittedOnDate;


    private String externalId;

    private String dateFormat;

    private String locale;

    private Map<String, Object> additionalProperties;

    private List<Map<String, Object>> disbursementData;

    private Map<String, Object> charges;

    private Map<String, Object> collateral;

    private Map<String, Object> guarantors;

    private String maxOutstandingLoanBalance;


}

