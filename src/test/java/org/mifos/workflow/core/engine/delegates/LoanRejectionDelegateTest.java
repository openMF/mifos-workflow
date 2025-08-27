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

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanRejectionDelegateTest {

    @Mock
    private FineractLoanService fineractLoanService;

    @Mock
    private DelegateExecution execution;

    @InjectMocks
    private LoanRejectionDelegate delegate;

    @BeforeEach
    void setUp() {
        lenient().when(execution.getVariable("rejectedOnDate")).thenReturn("2025-01-01");
        lenient().when(execution.getVariable("dateFormat")).thenReturn("yyyy-MM-dd");
        lenient().when(execution.getVariable("locale")).thenReturn("en");
        lenient().when(execution.getVariable("rejectedByUsername")).thenReturn(null);
        lenient().when(execution.getVariable("note")).thenReturn(null);

        lenient().when(execution.hasVariable("dateFormat")).thenReturn(true);
        lenient().when(execution.hasVariable("locale")).thenReturn(true);
        lenient().when(execution.hasVariable("rejectedByUsername")).thenReturn(false);
        lenient().when(execution.hasVariable("note")).thenReturn(false);
    }

    @Test
    void execute_Success() {
        when(execution.getProcessInstanceId()).thenReturn("p-lr");
        when(execution.getVariable("loanId")).thenReturn(1L);

        PostLoansLoanIdResponse resp = mock(PostLoansLoanIdResponse.class);
        when(fineractLoanService.performStateTransition(eq(1L), any(Map.class), eq("reject")))
                .thenReturn(resp);

        delegate.execute(execution);

        verify(execution).setVariable("loanRejectionSuccess", true);
        verify(execution).setVariable("loanRejectionMessage", "Loan rejected successfully");
        verify(execution).setVariable("loanStatus", "REJECTED");
    }

    @Test
    void execute_FineractError_Propagates() {
        when(execution.getProcessInstanceId()).thenReturn("p-lr");
        when(execution.getVariable("loanId")).thenReturn(1L);

        FineractApiException apiEx = new FineractApiException("bad", new RuntimeException("bad"), "reject", "1");
        when(fineractLoanService.performStateTransition(eq(1L), any(Map.class), eq("reject")))
                .thenThrow(apiEx);

        assertThrows(FineractApiException.class, () -> delegate.execute(execution));
    }

    @Test
    void execute_GenericError_ThrowsWorkflowException() {
        when(execution.getProcessInstanceId()).thenReturn("p-lr");
        when(execution.getVariable("loanId")).thenReturn(1L);

        when(fineractLoanService.performStateTransition(eq(1L), any(Map.class), eq("reject")))
                .thenThrow(new RuntimeException("x"));

        assertThrows(WorkflowException.class, () -> delegate.execute(execution));
    }
}


