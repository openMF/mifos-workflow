package org.mifos.workflow.core.engine.delegates;

import org.flowable.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mifos.fineract.client.models.DeleteLoansLoanIdResponse;
import org.mifos.workflow.exception.FineractApiException;
import org.mifos.workflow.exception.WorkflowException;
import org.mifos.workflow.service.fineract.loan.FineractLoanService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanCancellationDelegateTest {

    @Mock
    private FineractLoanService fineractLoanService;

    @Mock
    private DelegateExecution execution;

    @InjectMocks
    private LoanCancellationDelegate loanCancellationDelegate;

    private DeleteLoansLoanIdResponse mockResponse;

    @BeforeEach
    void setUp() {
        mockResponse = new DeleteLoansLoanIdResponse();
    }

    @Test
    void execute_Success() throws Exception {
        // Given
        Long loanId = 123L;
        String processInstanceId = "process-123";

        when(execution.getProcessInstanceId()).thenReturn(processInstanceId);
        when(execution.getVariable("loanId")).thenReturn(loanId);
        when(fineractLoanService.deleteLoan(loanId)).thenReturn(mockResponse);

        // When
        loanCancellationDelegate.execute(execution);

        // Then
        verify(execution).setVariable(eq("cancellationSuccessful"), eq(true));
        verify(execution).setVariable(eq("cancelledLoanId"), eq(loanId));
        verify(execution).setVariable(eq("cancelledResourceId"), any());
        verify(execution).setVariable(eq("cancelledClientId"), any());
        verify(execution).setVariable(eq("cancelledOfficeId"), any());
        verify(execution).setVariable(eq("cancellationTimestamp"), any());
        verify(fineractLoanService).deleteLoan(loanId);
    }

    @Test
    void execute_WithIntegerLoanId() throws Exception {
        // Given
        Integer loanId = 123;
        String processInstanceId = "process-123";

        when(execution.getProcessInstanceId()).thenReturn(processInstanceId);
        when(execution.getVariable("loanId")).thenReturn(loanId);
        when(fineractLoanService.deleteLoan(123L)).thenReturn(mockResponse);

        // When
        loanCancellationDelegate.execute(execution);

        // Then
        verify(execution).setVariable(eq("cancellationSuccessful"), eq(true));
        verify(execution).setVariable(eq("cancelledLoanId"), eq(123L));
        verify(execution).setVariable(eq("cancelledResourceId"), any());
        verify(execution).setVariable(eq("cancelledClientId"), any());
        verify(execution).setVariable(eq("cancelledOfficeId"), any());
        verify(execution).setVariable(eq("cancellationTimestamp"), any());
        verify(fineractLoanService).deleteLoan(123L);
    }

    @Test
    void execute_WithStringLoanId() throws Exception {
        // Given
        String loanId = "123";
        String processInstanceId = "process-123";

        when(execution.getProcessInstanceId()).thenReturn(processInstanceId);
        when(execution.getVariable("loanId")).thenReturn(loanId);
        when(fineractLoanService.deleteLoan(123L)).thenReturn(mockResponse);

        // When
        loanCancellationDelegate.execute(execution);

        // Then
        verify(execution).setVariable(eq("cancellationSuccessful"), eq(true));
        verify(execution).setVariable(eq("cancelledLoanId"), eq(123L));
        verify(execution).setVariable(eq("cancelledResourceId"), any());
        verify(execution).setVariable(eq("cancelledClientId"), any());
        verify(execution).setVariable(eq("cancelledOfficeId"), any());
        verify(execution).setVariable(eq("cancellationTimestamp"), any());
        verify(fineractLoanService).deleteLoan(123L);
    }

    @Test
    void execute_WithNullLoanId() throws Exception {
        // Given
        String processInstanceId = "process-123";

        when(execution.getProcessInstanceId()).thenReturn(processInstanceId);
        when(execution.getVariable("loanId")).thenReturn(null);

        // When & Then
        assertThrows(WorkflowException.class, () -> {
            loanCancellationDelegate.execute(execution);
        });

        verify(execution).setVariable("cancellationSuccessful", false);
        verify(execution).setVariable(eq("cancellationError"), any());
        verify(execution).setVariable(eq("errorMessage"), any());
    }

    @Test
    void execute_WithFineractApiException() throws Exception {
        // Given
        Long loanId = 123L;
        String processInstanceId = "process-123";
        String errorMessage = "Loan not found";

        when(execution.getProcessInstanceId()).thenReturn(processInstanceId);
        when(execution.getVariable("loanId")).thenReturn(loanId);
        when(fineractLoanService.deleteLoan(loanId)).thenThrow(new FineractApiException(errorMessage, new RuntimeException(errorMessage), "loan deletion", loanId.toString()));

        // When & Then
        assertThrows(FineractApiException.class, () -> {
            loanCancellationDelegate.execute(execution);
        });

        verify(execution).setVariable("cancellationSuccessful", false);
        verify(execution).setVariable(eq("cancellationError"), eq(errorMessage));
        verify(execution).setVariable(eq("errorMessage"), eq(errorMessage));
    }

    @Test
    void execute_WithGenericException() throws Exception {
        // Given
        Long loanId = 123L;
        String processInstanceId = "process-123";
        String errorMessage = "Unexpected error";

        when(execution.getProcessInstanceId()).thenReturn(processInstanceId);
        when(execution.getVariable("loanId")).thenReturn(loanId);
        when(fineractLoanService.deleteLoan(loanId)).thenThrow(new RuntimeException(errorMessage));

        // When & Then
        assertThrows(WorkflowException.class, () -> {
            loanCancellationDelegate.execute(execution);
        });

        verify(execution).setVariable("cancellationSuccessful", false);
        verify(execution).setVariable(eq("cancellationError"), eq(errorMessage));
        verify(execution).setVariable(eq("errorMessage"), eq(errorMessage));
    }
}
