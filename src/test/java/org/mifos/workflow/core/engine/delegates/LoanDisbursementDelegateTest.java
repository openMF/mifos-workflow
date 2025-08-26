package org.mifos.workflow.core.engine.delegates;

import org.flowable.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mifos.fineract.client.models.PostLoansLoanIdResponse;
import org.mifos.workflow.exception.FineractApiException;
import org.mifos.workflow.exception.WorkflowException;
import org.mifos.workflow.service.fineract.loan.FineractLoanService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanDisbursementDelegateTest {

    @Mock
    private FineractLoanService fineractLoanService;

    @Mock
    private DelegateExecution execution;

    @InjectMocks
    private LoanDisbursementDelegate delegate;

    @BeforeEach
    void setUp() {
        lenient().when(execution.getVariable("note")).thenReturn(null);
        lenient().when(execution.getVariable("disbursementData")).thenReturn(null);
        lenient().when(execution.getVariable("approvedAmount")).thenReturn(null);
        lenient().when(execution.getVariable("dateFormat")).thenReturn("yyyy-MM-dd");
        lenient().when(execution.getVariable("locale")).thenReturn("en");
        lenient().when(execution.getVariable("retryAttempt")).thenReturn(0);
        lenient().when(execution.getVariable("maxRetryAttempts")).thenReturn(3);
        lenient().when(execution.getVariable("autoRetryOnFailure")).thenReturn(true);
        lenient().when(execution.getVariable("disbursementOfficer")).thenReturn("officer1");
        lenient().when(execution.getVariable("transactionAmount")).thenReturn(1000.0);

        lenient().when(execution.hasVariable("dateFormat")).thenReturn(true);
        lenient().when(execution.hasVariable("locale")).thenReturn(true);
        lenient().when(execution.hasVariable("disbursementData")).thenReturn(false);
        lenient().when(execution.hasVariable("note")).thenReturn(false);
    }

    @Test
    void execute_Success() {
        // Setup basic variables
        when(execution.getProcessInstanceId()).thenReturn("p-ld");
        when(execution.getVariable("loanId")).thenReturn(1L);
        when(execution.getVariable("actualDisbursementDate")).thenReturn(LocalDate.parse("2024-01-10"));

        PostLoansLoanIdResponse response = mock(PostLoansLoanIdResponse.class);
        when(response.getResourceId()).thenReturn(123L);
        when(fineractLoanService.performStateTransition(eq(1L), any(Map.class), eq("disburse")))
                .thenReturn(response);

        // Execute
        delegate.execute(execution);

        // Verify
        verify(execution).setVariable("loanDisbursementSuccess", true);
        verify(execution).setVariable("loanDisbursementMessage", "Loan disbursed successfully");
        verify(execution).setVariable("loanStatus", "DISBURSED");
        verify(execution).setVariable("disbursementTransactionId", 123L);
        verify(execution).setVariable("disbursementCompletedBy", "officer1");
        verify(execution).setVariable("actualDisbursementAmount", 1000.0);
        verify(execution).setVariable("retryAttempt", 0);
        verify(execution).setVariable("lastError", null);
        verify(execution).setVariable("escalated", false);
    }

    @Test
    void execute_FineractError_Propagates() {
        when(execution.getProcessInstanceId()).thenReturn("p-ld");
        when(execution.getVariable("loanId")).thenReturn(1L);
        when(execution.getVariable("actualDisbursementDate")).thenReturn(LocalDate.now());

        FineractApiException apiEx = new FineractApiException("bad", new RuntimeException("bad"), "disburse", "1");
        when(fineractLoanService.performStateTransition(eq(1L), any(Map.class), eq("disburse")))
                .thenThrow(apiEx);

        FineractApiException thrown = assertThrows(FineractApiException.class, () -> delegate.execute(execution));
        assertEquals("bad", thrown.getMessage());
    }

    @Test
    void execute_MissingLoanId_ThrowsWorkflowException() {
        when(execution.getProcessInstanceId()).thenReturn("p-ld");
        when(execution.getVariable("loanId")).thenReturn(null);
        when(execution.getVariable("actualDisbursementDate")).thenReturn(LocalDate.now());

        WorkflowException ex = assertThrows(WorkflowException.class, () -> delegate.execute(execution));
        assertTrue(ex.getMessage().contains("Loan disbursement failed"));
        assertTrue(ex.getCause() instanceof IllegalArgumentException);
        assertTrue(ex.getCause().getMessage().contains("Loan ID is required"));
    }

    @Test
    void execute_MissingDisbursementDate_ThrowsWorkflowException() {
        when(execution.getProcessInstanceId()).thenReturn("p-ld");
        when(execution.getVariable("loanId")).thenReturn(1L);
        when(execution.getVariable("actualDisbursementDate")).thenReturn(null);

        WorkflowException ex = assertThrows(WorkflowException.class, () -> delegate.execute(execution));
        assertTrue(ex.getMessage().contains("Loan disbursement failed"));
        assertTrue(ex.getCause() instanceof IllegalArgumentException);
        assertTrue(ex.getCause().getMessage().contains("Disbursement date is required"));
    }
}


