package org.mifos.workflow.core.engine.delegates;

import org.flowable.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mifos.fineract.client.models.PostClientsClientIdResponse;
import org.mifos.workflow.dto.fineract.client.ClientAssignStaffRequestDTO;
import org.mifos.workflow.exception.FineractApiException;
import org.mifos.workflow.exception.WorkflowException;
import org.mifos.workflow.service.fineract.client.FineractClientService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StaffAssignmentDelegateTest {

    @Mock
    private FineractClientService fineractClientService;

    @Mock
    private DelegateExecution execution;

    @InjectMocks
    private StaffAssignmentDelegate delegate;

    @Test
    void execute_AssignsStaff_Success() {
        Long clientId = 10L;
        Long staffId = 55L;
        LocalDate assignmentDate = LocalDate.parse("2025-01-01");

        when(execution.getProcessInstanceId()).thenReturn("p-1");
        when(execution.getVariable("clientId")).thenReturn(clientId);
        when(execution.getVariable("staffId")).thenReturn(staffId);
        when(execution.getVariable("assignmentDate")).thenReturn(assignmentDate);

        PostClientsClientIdResponse apiResponse = mock(PostClientsClientIdResponse.class);
        when(apiResponse.getResourceId()).thenReturn(clientId);

        when(fineractClientService.assignStaff(eq(clientId), eq("assignStaff"), any(ClientAssignStaffRequestDTO.class)))
                .thenReturn(io.reactivex.rxjava3.core.Observable.just(apiResponse));

        delegate.execute(execution);

        verify(execution).setVariable("staffAssigned", true);
        verify(execution).setVariable("assignedStaffId", staffId);
        verify(execution).setVariable("assignmentDate", assignmentDate);
        verify(fineractClientService).assignStaff(eq(clientId), eq("assignStaff"), any(ClientAssignStaffRequestDTO.class));
    }

    @Test
    void execute_NoStaffId_SetsFlagFalseAndReturns() {
        Long clientId = 10L;
        when(execution.getProcessInstanceId()).thenReturn("p-1");
        when(execution.getVariable("clientId")).thenReturn(clientId);
        when(execution.getVariable("staffId")).thenReturn(null);

        delegate.execute(execution);

        verify(execution).setVariable("staffAssigned", false);
        verifyNoInteractions(fineractClientService);
    }

    @Test
    void execute_FineractApiException_ThrowsException() {
        Long clientId = 10L;
        Long staffId = 55L;
        when(execution.getProcessInstanceId()).thenReturn("p-1");
        when(execution.getVariable("clientId")).thenReturn(clientId);
        when(execution.getVariable("staffId")).thenReturn(staffId);

        FineractApiException apiEx = mock(FineractApiException.class);
        when(apiEx.isNotFound()).thenReturn(false); // Not a "not found" error
        when(apiEx.getMessage()).thenReturn("API Error");

        when(fineractClientService.assignStaff(eq(clientId), eq("assignStaff"), any(ClientAssignStaffRequestDTO.class)))
                .thenReturn(io.reactivex.rxjava3.core.Observable.error(apiEx));

        assertThrows(FineractApiException.class, () -> delegate.execute(execution));

        verify(execution).setVariable("staffAssigned", false);
        verify(execution).setVariable("errorMessage", "API Error");
    }

    @Test
    void execute_MissingClient_ThrowsWorkflowException() {
        when(execution.getProcessInstanceId()).thenReturn("p-1");
        when(execution.getVariable("clientId")).thenReturn(null);

        WorkflowException ex = assertThrows(WorkflowException.class, () -> delegate.execute(execution));
        assertTrue(ex.getMessage().contains("Staff assignment failed"));
        verify(execution).setVariable("staffAssigned", false);
        verify(execution).setVariable("errorMessage", "clientId is missing from process variables");
    }
}


