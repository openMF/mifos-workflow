package org.mifos.workflow.core.engine.delegates;

import org.flowable.engine.delegate.DelegateExecution;
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
class LoanApprovalDelegateTest {

    @Mock
    private FineractLoanService fineractLoanService;

    @Mock
    private DelegateExecution execution;

    @InjectMocks
    private LoanApprovalDelegate delegate;

    @Test
    void execute_Success() {
        when(execution.getProcessInstanceId()).thenReturn("p-la");
        when(execution.getVariable("loanId")).thenReturn(7L);
        when(execution.getVariable("approvedOnDate")).thenReturn("2024-02-01");
        when(execution.hasVariable("dateFormat")).thenReturn(false);
        when(execution.hasVariable("locale")).thenReturn(false);
        when(execution.hasVariable("approvedByUsername")).thenReturn(false);
        when(execution.hasVariable("note")).thenReturn(false);

        PostLoansLoanIdResponse resp = mock(PostLoansLoanIdResponse.class);
        when(fineractLoanService.performStateTransition(eq(7L), any(Map.class), eq("approve")))
            .thenReturn(resp);
        delegate.execute(execution);

        verify(execution).setVariable("loanApprovalSuccess", true);
        verify(execution).setVariable("loanApprovalMessage", "Loan approved successfully");
        verify(execution).setVariable("loanStatus", "APPROVED");
    }

    @Test
    void execute_FineractError_Propagates() {
        when(execution.getProcessInstanceId()).thenReturn("p-la");
        when(execution.getVariable("loanId")).thenReturn(7L);
        when(execution.getVariable("approvedOnDate")).thenReturn("2024-02-01");
        when(execution.hasVariable("dateFormat")).thenReturn(false);
        when(execution.hasVariable("locale")).thenReturn(false);
        when(execution.hasVariable("approvedByUsername")).thenReturn(false);
        when(execution.hasVariable("note")).thenReturn(false);
        
        FineractApiException apiEx = new FineractApiException("bad", new RuntimeException("bad"), "approve", "7");
        when(fineractLoanService.performStateTransition(eq(7L), any(Map.class), eq("approve")))
            .thenThrow(apiEx);
        assertThrows(FineractApiException.class, () -> delegate.execute(execution));
    }

    @Test
    void execute_GenericError_ThrowsWorkflowException() {
        when(execution.getProcessInstanceId()).thenReturn("p-la");
        when(execution.getVariable("loanId")).thenReturn(7L);
        when(execution.getVariable("approvedOnDate")).thenReturn("2024-02-01");
        when(execution.hasVariable("dateFormat")).thenReturn(false);
        when(execution.hasVariable("locale")).thenReturn(false);
        when(execution.hasVariable("approvedByUsername")).thenReturn(false);
        when(execution.hasVariable("note")).thenReturn(false);
        
        when(fineractLoanService.performStateTransition(eq(7L), any(Map.class), eq("approve")))
            .thenThrow(new RuntimeException("x"));
        assertThrows(WorkflowException.class, () -> delegate.execute(execution));
    }
}


