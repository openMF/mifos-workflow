package org.mifos.workflow.core.engine.delegates;

import org.flowable.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mifos.fineract.client.models.PostLoansResponse;
import org.mifos.workflow.exception.FineractApiException;
import org.mifos.workflow.service.fineract.loan.FineractLoanService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanCreationDelegateTest {

    @Mock
    private FineractLoanService fineractLoanService;

    @Mock
    private DelegateExecution execution;

    @InjectMocks
    private LoanCreationDelegate delegate;

    @Test
    void execute_Success() {
        when(execution.getProcessInstanceId()).thenReturn("p-lc");
        when(execution.getVariable("clientId")).thenReturn(1L);
        when(execution.getVariable("productId")).thenReturn(2L);
        when(execution.getVariable("principal")).thenReturn(1000.0);
        when(execution.getVariable("loanTermFrequency")).thenReturn(12);
        when(execution.getVariable("loanTermFrequencyType")).thenReturn(2);
        when(execution.getVariable("loanPurposeId")).thenReturn(1);
        when(execution.getVariable("interestRatePerPeriod")).thenReturn(10.0);
        when(execution.getVariable("interestRateFrequencyType")).thenReturn(2);
        when(execution.getVariable("amortizationType")).thenReturn(1);
        when(execution.getVariable("interestType")).thenReturn(0);
        when(execution.getVariable("interestCalculationPeriodType")).thenReturn(1);
        when(execution.getVariable("transactionProcessingStrategyCode")).thenReturn("default");
        when(execution.getVariable("numberOfRepayments")).thenReturn(12);
        when(execution.getVariable("repaymentEvery")).thenReturn(1);
        when(execution.getVariable("repaymentFrequencyType")).thenReturn(2);
        when(execution.getVariable("expectedDisbursementDate")).thenReturn("01 Jan 2025");
        when(execution.getVariable("submittedOnDate")).thenReturn("01 Jan 2025");
        when(execution.getVariable("loanType")).thenReturn("INDIVIDUAL");

        PostLoansResponse resp = new PostLoansResponse();
        when(fineractLoanService.createLoan(any(Map.class), anyString())).thenReturn(resp);

        delegate.execute(execution);

        verify(execution).setVariable(eq("loanCreationSuccess"), eq(true));
        verify(execution).setVariable(eq("loanCreationMessage"), any());
    }

    @Test
    void execute_FineractError_Propagates() {
        when(execution.getProcessInstanceId()).thenReturn("p-lc");
        when(execution.getVariable("clientId")).thenReturn(1L);
        when(execution.getVariable("productId")).thenReturn(2L);
        when(execution.getVariable("principal")).thenReturn(1000.0);
        when(execution.getVariable("loanTermFrequency")).thenReturn(12);
        when(execution.getVariable("loanTermFrequencyType")).thenReturn(2);
        when(execution.getVariable("loanPurposeId")).thenReturn(1);
        when(execution.getVariable("interestRatePerPeriod")).thenReturn(10.0);
        when(execution.getVariable("interestRateFrequencyType")).thenReturn(2);
        when(execution.getVariable("amortizationType")).thenReturn(1);
        when(execution.getVariable("interestType")).thenReturn(0);
        when(execution.getVariable("interestCalculationPeriodType")).thenReturn(1);
        when(execution.getVariable("transactionProcessingStrategyCode")).thenReturn("default");
        when(execution.getVariable("numberOfRepayments")).thenReturn(12);
        when(execution.getVariable("repaymentEvery")).thenReturn(1);
        when(execution.getVariable("repaymentFrequencyType")).thenReturn(2);
        when(execution.getVariable("expectedDisbursementDate")).thenReturn("01 Jan 2025");
        when(execution.getVariable("submittedOnDate")).thenReturn("01 Jan 2025");
        when(execution.getVariable("loanType")).thenReturn("INDIVIDUAL");

        FineractApiException apiEx = new FineractApiException("bad", new RuntimeException("bad"), "create loan", "1");
        when(fineractLoanService.createLoan(any(Map.class), anyString())).thenThrow(apiEx);

        assertThrows(FineractApiException.class, () -> delegate.execute(execution));
    }
}




