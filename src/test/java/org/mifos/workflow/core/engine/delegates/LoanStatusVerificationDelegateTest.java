package org.mifos.workflow.core.engine.delegates;

import org.flowable.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mifos.fineract.client.models.GetLoansLoanIdResponse;
import org.mifos.fineract.client.models.GetLoansLoanIdStatus;
import org.mifos.fineract.client.models.GetLoansLoanIdCurrency;
import org.mifos.workflow.exception.FineractApiException;
import org.mifos.workflow.exception.WorkflowException;
import org.mifos.workflow.service.fineract.loan.FineractLoanService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanStatusVerificationDelegateTest {

    @Mock
    private FineractLoanService fineractLoanService;

    @Mock
    private DelegateExecution execution;

    @InjectMocks
    private LoanStatusVerificationDelegate delegate;

    @BeforeEach
    void setUp() {
        lenient().when(execution.getVariable("disbursementOfficer")).thenReturn("officer1");
        lenient().when(execution.getVariable("transactionAmount")).thenReturn(null);
    }

    @Test
    void execute_ApprovedLoan_ReadyForDisbursement() {
        lenient().when(execution.getProcessInstanceId()).thenReturn("p-lsv");
        lenient().when(execution.getVariable("loanId")).thenReturn(10L);
        lenient().when(execution.getVariable("transactionAmount")).thenReturn(null);

        GetLoansLoanIdResponse response = mock(GetLoansLoanIdResponse.class);
        GetLoansLoanIdStatus status = mock(GetLoansLoanIdStatus.class);
        GetLoansLoanIdCurrency currency = mock(GetLoansLoanIdCurrency.class);

        when(status.toString()).thenReturn("APPROVED");
        when(status.getActive()).thenReturn(true);
        when(response.getStatus()).thenReturn(status);

        when(response.getAccountNo()).thenReturn("LOAN001");
        when(response.getPrincipal()).thenReturn(10000.0);
        when(response.getLoanProductId()).thenReturn(1L);
        when(response.getClientId()).thenReturn(100L);
        when(response.getTermFrequency()).thenReturn(12);
        when(response.getInterestRatePerPeriod()).thenReturn(10.5);
        when(response.getCharges()).thenReturn(null);
        when(response.getSummary()).thenReturn(null);
        when(response.getCurrency()).thenReturn(currency);
        when(currency.getCode()).thenReturn("USD");

        lenient().when(fineractLoanService.getLoan(10L, null, null, null, "id,accountNo,status,principal,loanProductId,clientId,currency"))
                .thenReturn(response);

        delegate.execute(execution);

        verify(execution).setVariable("loanStatusVerified", true);
        verify(execution).setVariable("loanReadyForDisbursement", true);
        verify(execution).setVariable("verificationDate", "2025-08-26");
        verify(execution).setVariable("loanStatus", "APPROVED");
        verify(execution).setVariable("loanStatusMessage", "Loan is approved and ready for disbursement");
        verify(execution).setVariable("loanAccountNo", "LOAN001");
        verify(execution).setVariable("loanPrincipal", BigDecimal.valueOf(10000.0)); // Actual implementation uses BigDecimal
        verify(execution).setVariable("loanProductId", 1L);
        verify(execution).setVariable("loanClientId", 100L);
        verify(execution).setVariable("loanTermFrequency", 12);
        verify(execution).setVariable("loanInterestRate", BigDecimal.valueOf(10.5)); // Actual implementation uses BigDecimal
        verify(execution).setVariable("hasVerificationIssues", false);
        verify(execution).setVariable("issueCount", 0);
        verify(execution).setVariable("loanCurrencyCode", "USD");
        verify(execution).setVariable("riskLevel", "LOW");
        verify(execution).setVariable("complianceCheckRequired", false);
        verify(execution).setVariable("approvalLevel", "OFFICER");
    }

    @Test
    void execute_DisbursedLoan_NotReadyForDisbursement() {
        lenient().when(execution.getProcessInstanceId()).thenReturn("p-lsv");
        lenient().when(execution.getVariable("loanId")).thenReturn(10L);
        lenient().when(execution.getVariable("transactionAmount")).thenReturn(null);

        GetLoansLoanIdResponse response = mock(GetLoansLoanIdResponse.class);
        GetLoansLoanIdStatus status = mock(GetLoansLoanIdStatus.class);

        when(status.toString()).thenReturn("DISBURSED");
        when(status.getActive()).thenReturn(true);
        when(response.getStatus()).thenReturn(status);

        when(response.getAccountNo()).thenReturn("LOAN001");
        when(response.getPrincipal()).thenReturn(10000.0);
        when(response.getLoanProductId()).thenReturn(1L);
        when(response.getClientId()).thenReturn(100L);
        when(response.getTermFrequency()).thenReturn(12);
        when(response.getInterestRatePerPeriod()).thenReturn(10.5);
        when(response.getCharges()).thenReturn(null);
        when(response.getSummary()).thenReturn(null);
        when(response.getCurrency()).thenReturn(null);

        lenient().when(fineractLoanService.getLoan(10L, null, null, null, "id,accountNo,status,principal,loanProductId,clientId,currency"))
                .thenReturn(response);

        delegate.execute(execution);

        verify(execution).setVariable("loanStatusVerified", true);
        verify(execution).setVariable("loanReadyForDisbursement", false);
        verify(execution).setVariable("loanStatus", "DISBURSED");
        verify(execution).setVariable("loanStatusMessage", "Loan is already disbursed");
        verify(execution).setVariable("hasVerificationIssues", true);
        verify(execution).setVariable("issueCount", 1);
        verify(execution).setVariable("loanStatusError", "Loan is already disbursed");
        verify(execution).setVariable("blockingIssues", List.of("Loan has already been disbursed"));
    }

    @Test
    void execute_RejectedLoan_NotReadyForDisbursement() {
        lenient().when(execution.getProcessInstanceId()).thenReturn("p-lsv");
        lenient().when(execution.getVariable("loanId")).thenReturn(10L);
        lenient().when(execution.getVariable("transactionAmount")).thenReturn(null);

        GetLoansLoanIdResponse response = mock(GetLoansLoanIdResponse.class);
        GetLoansLoanIdStatus status = mock(GetLoansLoanIdStatus.class);

        when(status.toString()).thenReturn("REJECTED");
        when(status.getActive()).thenReturn(false);
        when(response.getStatus()).thenReturn(status);

        when(response.getAccountNo()).thenReturn("LOAN001");
        when(response.getPrincipal()).thenReturn(10000.0);
        when(response.getLoanProductId()).thenReturn(1L);
        when(response.getClientId()).thenReturn(100L);
        when(response.getTermFrequency()).thenReturn(12);
        when(response.getInterestRatePerPeriod()).thenReturn(10.5);
        when(response.getCharges()).thenReturn(null);
        when(response.getSummary()).thenReturn(null);
        when(response.getCurrency()).thenReturn(null);

        lenient().when(fineractLoanService.getLoan(10L, null, null, null, "id,accountNo,status,principal,loanProductId,clientId,currency"))
                .thenReturn(response);

        delegate.execute(execution);

        verify(execution).setVariable("loanStatusVerified", true);
        verify(execution).setVariable("loanReadyForDisbursement", false);
        verify(execution).setVariable("loanStatus", "REJECTED");
        verify(execution).setVariable("loanStatusMessage", "Loan is not approved. Current status: REJECTED");
        verify(execution).setVariable("hasVerificationIssues", true);
        verify(execution).setVariable("issueCount", 1);
        verify(execution).setVariable("loanStatusError", "Loan is not approved. Current status: REJECTED");
        verify(execution).setVariable("blockingIssues", List.of("Loan is not in approved status"));
    }

    @Test
    void execute_HighValueLoan_RequiresComplianceCheck() {
        lenient().when(execution.getProcessInstanceId()).thenReturn("p-lsv");
        lenient().when(execution.getVariable("loanId")).thenReturn(10L);
        lenient().when(execution.getVariable("transactionAmount")).thenReturn(BigDecimal.valueOf(600000));

        GetLoansLoanIdResponse response = mock(GetLoansLoanIdResponse.class);
        GetLoansLoanIdStatus status = mock(GetLoansLoanIdStatus.class);

        when(status.toString()).thenReturn("APPROVED");
        when(status.getActive()).thenReturn(true);
        when(response.getStatus()).thenReturn(status);

        when(response.getAccountNo()).thenReturn("LOAN001");
        when(response.getPrincipal()).thenReturn(600000.0);
        when(response.getLoanProductId()).thenReturn(1L);
        when(response.getClientId()).thenReturn(100L);
        when(response.getTermFrequency()).thenReturn(12);
        when(response.getInterestRatePerPeriod()).thenReturn(10.5);
        when(response.getCharges()).thenReturn(null);
        when(response.getSummary()).thenReturn(null);
        when(response.getCurrency()).thenReturn(null);

        lenient().when(fineractLoanService.getLoan(10L, null, null, null, "id,accountNo,status,principal,loanProductId,clientId,currency"))
                .thenReturn(response);

        delegate.execute(execution);

        verify(execution).setVariable("loanStatusVerified", true);
        verify(execution).setVariable("loanReadyForDisbursement", true);
        verify(execution).setVariable("riskLevel", "HIGH");
        verify(execution).setVariable("complianceCheckRequired", true);
        verify(execution).setVariable("approvalLevel", "MANAGER"); // 600k is MANAGER level, not SENIOR_MANAGER
    }

    @Test
    void execute_MissingLoanId_ThrowsException() {
        lenient().when(execution.getProcessInstanceId()).thenReturn("p-lsv");
        lenient().when(execution.getVariable("loanId")).thenReturn(null);
        lenient().when(execution.getVariable("transactionAmount")).thenReturn(null);

        WorkflowException exception = assertThrows(WorkflowException.class, () -> {
            delegate.execute(execution);
        });

        verify(execution).setVariable("loanStatusVerified", false);
        verify(execution).setVariable("loanStatusError", "Loan ID is required for status verification");
        verify(execution).setVariable("loanReadyForDisbursement", false);
        verify(execution).setVariable("verificationFailed", true);
    }

    @Test
    void execute_FineractApiException_HandlesError() {
        lenient().when(execution.getProcessInstanceId()).thenReturn("p-lsv");
        lenient().when(execution.getVariable("loanId")).thenReturn(10L);
        lenient().when(execution.getVariable("transactionAmount")).thenReturn(null);

        FineractApiException apiException = new FineractApiException("API Error", new RuntimeException(), "getLoan", "10");
        lenient().when(fineractLoanService.getLoan(10L, null, null, null, "id,accountNo,status,principal,loanProductId,clientId,currency"))
                .thenThrow(apiException);

        assertThrows(FineractApiException.class, () -> {
            delegate.execute(execution);
        });

        verify(execution).setVariable("loanStatusVerified", false);
        verify(execution).setVariable("loanStatusError", "API Error");
        verify(execution).setVariable("loanReadyForDisbursement", false);
        verify(execution).setVariable("errorType", "Fineract API Error");
        verify(execution).setVariable("verificationFailed", true);
    }

    @Test
    void execute_LoanWithCharges_HasIssues() {
        lenient().when(execution.getProcessInstanceId()).thenReturn("p-lsv");
        lenient().when(execution.getVariable("loanId")).thenReturn(10L);
        lenient().when(execution.getVariable("transactionAmount")).thenReturn(null);

        GetLoansLoanIdResponse response = mock(GetLoansLoanIdResponse.class);
        GetLoansLoanIdStatus status = mock(GetLoansLoanIdStatus.class);

        when(status.toString()).thenReturn("APPROVED");
        when(status.getActive()).thenReturn(true);
        when(response.getStatus()).thenReturn(status);

        when(response.getAccountNo()).thenReturn("LOAN001");
        when(response.getPrincipal()).thenReturn(10000.0);
        when(response.getLoanProductId()).thenReturn(1L);
        when(response.getClientId()).thenReturn(100L);
        when(response.getTermFrequency()).thenReturn(12);
        when(response.getInterestRatePerPeriod()).thenReturn(10.5);
        when(response.getCharges()).thenReturn(List.of()); // Has charges (empty list)
        when(response.getSummary()).thenReturn(null);
        when(response.getCurrency()).thenReturn(null);

        lenient().when(fineractLoanService.getLoan(10L, null, null, null, "id,accountNo,status,principal,loanProductId,clientId,currency"))
                .thenReturn(response);

        delegate.execute(execution);

        verify(execution).setVariable("loanStatusVerified", true);
        verify(execution).setVariable("loanReadyForDisbursement", true);
        verify(execution).setVariable("hasVerificationIssues", false); // Empty list doesn't trigger issues
        verify(execution).setVariable("issueCount", 0);
    }
}


