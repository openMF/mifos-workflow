package org.mifos.workflow.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mifos.workflow.core.model.ProcessInstance;
import org.mifos.workflow.core.model.ProcessStatus;
import org.mifos.workflow.core.model.TaskInfo;
import org.mifos.workflow.dto.fineract.loan.LoanCancellationRequestDTO;
import org.mifos.workflow.service.WorkflowService;
import org.mifos.workflow.util.ApiResponse;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanCancellationControllerTest {

    @Mock
    private WorkflowService workflowService;

    @InjectMocks
    private LoanCancellationController loanCancellationController;

    private LoanCancellationRequestDTO validCancellationRequest;
    private ProcessInstance mockProcessInstance;
    private List<TaskInfo> mockTasks;
    private ProcessStatus mockProcessStatus;

    @BeforeEach
    void setUp() {
        validCancellationRequest = new LoanCancellationRequestDTO();
        validCancellationRequest.setLoanId(1L);
        validCancellationRequest.setExternalId("CANCEL-001");
        validCancellationRequest.setCancellationReason("Client requested cancellation");
        validCancellationRequest.setCancellationDate(LocalDate.parse("2024-01-15"));
        validCancellationRequest.setCancelledBy("manager1");
        validCancellationRequest.setNotes("Cancellation approved by manager");
        validCancellationRequest.setLoanOfficer("officer1");
        validCancellationRequest.setAssignee("system");
        validCancellationRequest.setApprover("manager");

        mockProcessInstance = ProcessInstance.builder()
                .id("process-123")
                .processDefinitionId("loan-cancellation")
                .status("ACTIVE")
                .build();

        mockTasks = Arrays.asList(
                createTaskInfo("task-1", "Review Cancellation", "process-123"),
                createTaskInfo("task-2", "Approve Cancellation", "process-123"),
                createTaskInfo("task-3", "Process Cancellation", "process-123")
        );

        mockProcessStatus = new ProcessStatus();
        mockProcessStatus.setProcessInstanceId("process-123");
        mockProcessStatus.setStatus("ACTIVE");
        mockProcessStatus.setCurrentActivityName("Review Cancellation");
    }

    private TaskInfo createTaskInfo(String taskId, String taskName, String processId) {
        return TaskInfo.builder()
                .taskId(taskId)
                .name(taskName)
                .processId(processId)
                .assignee("system")
                .build();
    }

    @Test
    void startLoanCancellation_Success() {
        // Given
        when(workflowService.startProcess(eq("loan-cancellation"), any()))
                .thenReturn(mockProcessInstance);

        // When
        ResponseEntity<ProcessInstance> response = loanCancellationController.startLoanCancellation(validCancellationRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("process-123", response.getBody().getId());
        verify(workflowService).startProcess(eq("loan-cancellation"), any());
    }

    @Test
    void startLoanCancellation_WithNullOptionalFields_Success() {
        // Given
        validCancellationRequest.setLoanOfficer(null);
        validCancellationRequest.setAssignee(null);
        validCancellationRequest.setApprover(null);

        when(workflowService.startProcess(eq("loan-cancellation"), any()))
                .thenReturn(mockProcessInstance);

        // When
        ResponseEntity<ProcessInstance> response = loanCancellationController.startLoanCancellation(validCancellationRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(workflowService).startProcess(eq("loan-cancellation"), any());
    }

    @Test
    void startLoanCancellation_WithNullCancelledBy_Success() {
        // Given
        validCancellationRequest.setCancelledBy(null);

        when(workflowService.startProcess(eq("loan-cancellation"), any()))
                .thenReturn(mockProcessInstance);

        // When
        ResponseEntity<ProcessInstance> response = loanCancellationController.startLoanCancellation(validCancellationRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(workflowService).startProcess(eq("loan-cancellation"), any());
    }

    @Test
    void completeTask_Success() {
        // Given
        String taskId = "task-1";
        Map<String, Object> variables = new HashMap<>();
        variables.put("approved", true);
        variables.put("notes", "Task completed");

        when(workflowService.getPendingTasks("system")).thenReturn(mockTasks);
        doNothing().when(workflowService).setProcessVariables(anyString(), any());
        doNothing().when(workflowService).completeTask(eq(taskId), any());

        // When
        ResponseEntity<ApiResponse<Void>> response = loanCancellationController.completeTask(taskId, variables);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Task completed successfully", response.getBody().getMessage());
        verify(workflowService).completeTask(taskId, variables);
    }

    @Test
    void completeTask_TaskNotFound_ReturnsSuccess() {
        // Given
        String taskId = "non-existent-task";
        Map<String, Object> variables = new HashMap<>();

        when(workflowService.getPendingTasks("system")).thenReturn(mockTasks);

        // When
        ResponseEntity<ApiResponse<Void>> response = loanCancellationController.completeTask(taskId, variables);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Task completed successfully", response.getBody().getMessage());
        verify(workflowService).completeTask(taskId, variables);
    }

    @Test
    void getProcessTasks_Success() {
        // Given
        String processInstanceId = "process-123";
        when(workflowService.getPendingTasksForProcess(processInstanceId))
                .thenReturn(mockTasks);

        // When
        ResponseEntity<List<TaskInfo>> response = loanCancellationController.getProcessTasks(processInstanceId);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().size());
        assertEquals("task-1", response.getBody().get(0).getTaskId());
        verify(workflowService).getPendingTasksForProcess(processInstanceId);
    }

    @Test
    void getProcessStatus_Success() {
        // Given
        String processInstanceId = "process-123";
        when(workflowService.getProcessStatus(processInstanceId))
                .thenReturn(mockProcessStatus);

        // When
        ResponseEntity<ProcessStatus> response = loanCancellationController.getProcessStatus(processInstanceId);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("process-123", response.getBody().getProcessInstanceId());
        assertEquals("ACTIVE", response.getBody().getStatus());
        verify(workflowService).getProcessStatus(processInstanceId);
    }

    @Test
    void startLoanCancellation_WithDifferentCancellationReasons_Success() {
        // Given
        String[] reasons = {
                "Client requested cancellation",
                "Insufficient documentation",
                "Policy violation",
                "Duplicate application",
                "Client withdrawal"
        };

        when(workflowService.startProcess(eq("loan-cancellation"), any()))
                .thenReturn(mockProcessInstance);

        for (String reason : reasons) {
            validCancellationRequest.setCancellationReason(reason);

            // When
            ResponseEntity<ProcessInstance> response = loanCancellationController.startLoanCancellation(validCancellationRequest);

            // Then
            assertNotNull(response);
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
        }

        // Verify the service was called the expected number of times
        verify(workflowService, times(reasons.length)).startProcess(eq("loan-cancellation"), any());
    }

    @Test
    void startLoanCancellation_WithDifferentAssignees_Success() {
        // Given
        String[] assignees = {"system", "manager", "officer", "admin"};

        when(workflowService.startProcess(eq("loan-cancellation"), any()))
                .thenReturn(mockProcessInstance);

        for (String assignee : assignees) {
            validCancellationRequest.setAssignee(assignee);

            // When
            ResponseEntity<ProcessInstance> response = loanCancellationController.startLoanCancellation(validCancellationRequest);

            // Then
            assertNotNull(response);
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
        }

        // Verify the service was called the expected number of times
        verify(workflowService, times(assignees.length)).startProcess(eq("loan-cancellation"), any());
    }

    @Test
    void startLoanCancellation_WithDifferentApprovers_Success() {
        // Given
        String[] approvers = {"manager", "senior_manager", "director", "ceo"};

        when(workflowService.startProcess(eq("loan-cancellation"), any()))
                .thenReturn(mockProcessInstance);

        for (String approver : approvers) {
            validCancellationRequest.setApprover(approver);

            // When
            ResponseEntity<ProcessInstance> response = loanCancellationController.startLoanCancellation(validCancellationRequest);

            // Then
            assertNotNull(response);
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
        }

        // Verify the service was called the expected number of times
        verify(workflowService, times(approvers.length)).startProcess(eq("loan-cancellation"), any());
    }

    @Test
    void completeTask_WithDifferentVariables_Success() {
        // Given
        String taskId = "task-1";
        Map<String, Object> variables = new HashMap<>();
        variables.put("approved", false);
        variables.put("rejectionReason", "Insufficient documentation");
        variables.put("notes", "Cancellation rejected");

        when(workflowService.getPendingTasks("system")).thenReturn(mockTasks);
        doNothing().when(workflowService).setProcessVariables(anyString(), any());
        doNothing().when(workflowService).completeTask(eq(taskId), any());

        // When
        ResponseEntity<ApiResponse<Void>> response = loanCancellationController.completeTask(taskId, variables);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Task completed successfully", response.getBody().getMessage());
        verify(workflowService).completeTask(taskId, variables);
    }

    @Test
    void startLoanCancellation_WithExternalId_Success() {
        // Given
        validCancellationRequest.setExternalId("EXT-CANCEL-001");

        when(workflowService.startProcess(eq("loan-cancellation"), any()))
                .thenReturn(mockProcessInstance);

        // When
        ResponseEntity<ProcessInstance> response = loanCancellationController.startLoanCancellation(validCancellationRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(workflowService).startProcess(eq("loan-cancellation"), any());
    }

    @Test
    void startLoanCancellation_WithNotes_Success() {
        // Given
        validCancellationRequest.setNotes("Detailed cancellation notes with additional information");

        when(workflowService.startProcess(eq("loan-cancellation"), any()))
                .thenReturn(mockProcessInstance);

        // When
        ResponseEntity<ProcessInstance> response = loanCancellationController.startLoanCancellation(validCancellationRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(workflowService).startProcess(eq("loan-cancellation"), any());
    }

    @Test
    void startLoanCancellation_WithNullNotes_Success() {
        // Given
        validCancellationRequest.setNotes(null);

        when(workflowService.startProcess(eq("loan-cancellation"), any()))
                .thenReturn(mockProcessInstance);

        // When
        ResponseEntity<ProcessInstance> response = loanCancellationController.startLoanCancellation(validCancellationRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(workflowService).startProcess(eq("loan-cancellation"), any());
    }

    @Test
    void startLoanCancellation_WithNullExternalId_Success() {
        // Given
        validCancellationRequest.setExternalId(null);

        when(workflowService.startProcess(eq("loan-cancellation"), any()))
                .thenReturn(mockProcessInstance);

        // When
        ResponseEntity<ProcessInstance> response = loanCancellationController.startLoanCancellation(validCancellationRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(workflowService).startProcess(eq("loan-cancellation"), any());
    }
}
