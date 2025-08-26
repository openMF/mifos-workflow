package org.mifos.workflow.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mifos.workflow.core.model.ProcessInstance;
import org.mifos.workflow.core.model.ProcessStatus;
import org.mifos.workflow.core.model.TaskInfo;
import org.mifos.workflow.dto.fineract.loan.LoanApprovalRequestDTO;
import org.mifos.workflow.dto.fineract.loan.LoanCreateRequestDTO;
import org.mifos.workflow.dto.fineract.loan.LoanRejectionRequestDTO;
import org.mifos.workflow.service.WorkflowService;
import org.mifos.workflow.util.ApiResponse;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanOriginationControllerTest {

    @Mock
    private WorkflowService workflowService;

    @InjectMocks
    private LoanOriginationController loanOriginationController;

    private LoanCreateRequestDTO validLoanRequest;
    private ProcessInstance mockProcessInstance;
    private List<TaskInfo> mockTasks;
    private ProcessStatus mockProcessStatus;

    @BeforeEach
    void setUp() {
        validLoanRequest = LoanCreateRequestDTO.builder()
                .clientId(1L)
                .productId(1L)
                .principal(new BigDecimal("10000.00"))
                .loanTermFrequency(12)
                .loanTermFrequencyType(2)
                .loanType("individual")
                .loanPurposeId(1L)
                .interestRatePerPeriod(new BigDecimal("10.0"))
                .interestRateFrequencyType(2)
                .amortizationType(1)
                .interestType(1)
                .interestCalculationPeriodType(1)
                .transactionProcessingStrategyId(1L)
                .loanDate(LocalDate.parse("2024-01-01"))
                .submittedOnDate(LocalDate.parse("2024-01-01"))
                .externalId("EXT-001")
                .build();

        mockProcessInstance = ProcessInstance.builder()
                .id("process-123")
                .processDefinitionId("loan-origination")
                .status("ACTIVE")
                .build();

        mockTasks = Arrays.asList(
                createTaskInfo("task-1", "Review Application", "process-123"),
                createTaskInfo("task-2", "Approve Loan", "process-123")
        );

        mockProcessStatus = new ProcessStatus();
        mockProcessStatus.setProcessInstanceId("process-123");
        mockProcessStatus.setStatus("ACTIVE");
        mockProcessStatus.setCurrentActivityName("Review Application");
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
    void startLoanOrigination_Success() {
        // Given
        when(workflowService.startProcess(eq("loan-origination"), any()))
                .thenReturn(mockProcessInstance);

        // When
        ResponseEntity<ProcessInstance> response = loanOriginationController.startLoanOrigination(validLoanRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("process-123", response.getBody().getId());
        verify(workflowService).startProcess(eq("loan-origination"), any());
    }

    @Test
    void startLoanOrigination_WithAdditionalProperties_Success() {
        // Given
        Map<String, Object> additionalProps = new HashMap<>();
        additionalProps.put("customField", "customValue");
        additionalProps.put("numericField", "1,000.50");
        validLoanRequest.setAdditionalProperties(additionalProps);

        when(workflowService.startProcess(eq("loan-origination"), any()))
                .thenReturn(mockProcessInstance);

        // When
        ResponseEntity<ProcessInstance> response = loanOriginationController.startLoanOrigination(validLoanRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(workflowService).startProcess(eq("loan-origination"), any());
    }

    @Test
    void startLoanOrigination_WithDisbursementData_Success() {
        // Given
        Map<String, Object> disbursement = new HashMap<>();
        disbursement.put("disbursementDate", LocalDate.parse("2024-01-15"));
        disbursement.put("amount", new BigDecimal("10000.00"));
        validLoanRequest.setDisbursementData(Arrays.asList(disbursement));

        when(workflowService.startProcess(eq("loan-origination"), any()))
                .thenReturn(mockProcessInstance);

        // When
        ResponseEntity<ProcessInstance> response = loanOriginationController.startLoanOrigination(validLoanRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(workflowService).startProcess(eq("loan-origination"), any());
    }

    @Test
    void approveLoan_Success() {
        // Given
        LoanApprovalRequestDTO approvalRequest = new LoanApprovalRequestDTO();
        approvalRequest.setLoanId(10L);
        approvalRequest.setExternalId("EXT-001");
        approvalRequest.setApprovedOnDate(LocalDate.parse("2024-01-02"));
        approvalRequest.setApprovedByUsername("manager");
        approvalRequest.setNote("Approved after review");

        when(workflowService.startProcess(eq("loan-approval"), any()))
                .thenReturn(mockProcessInstance);

        // When
        ResponseEntity<ProcessInstance> response = loanOriginationController.approveLoan(approvalRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(workflowService).startProcess(eq("loan-approval"), any());
    }

    @Test
    void rejectLoan_Success() {
        // Given
        LoanRejectionRequestDTO rejectionRequest = new LoanRejectionRequestDTO();
        rejectionRequest.setLoanId(10L);
        rejectionRequest.setExternalId("EXT-001");
        rejectionRequest.setRejectedOnDate(LocalDate.parse("2024-01-02"));
        rejectionRequest.setRejectedReasonId(1L);
        rejectionRequest.setRejectedByUsername("manager");
        rejectionRequest.setNote("Insufficient documentation");

        when(workflowService.startProcess(eq("loan-rejection"), any()))
                .thenReturn(mockProcessInstance);

        // When
        ResponseEntity<ProcessInstance> response = loanOriginationController.rejectLoan(rejectionRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(workflowService).startProcess(eq("loan-rejection"), any());
    }

    @Test
    void getProcessTasks_Success() {
        // Given
        String processInstanceId = "process-123";
        when(workflowService.getPendingTasksForProcess(processInstanceId))
                .thenReturn(mockTasks);

        // When
        ResponseEntity<List<TaskInfo>> response = loanOriginationController.getTasksForProcess(processInstanceId);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
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
        ResponseEntity<ProcessStatus> response = loanOriginationController.getProcessStatus(processInstanceId);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("process-123", response.getBody().getProcessInstanceId());
        assertEquals("ACTIVE", response.getBody().getStatus());
        verify(workflowService).getProcessStatus(processInstanceId);
    }

    @Test
    void completeTask_Success() {
        // Given
        String taskId = "task-1";
        Map<String, Object> variables = new HashMap<>();
        variables.put("approved", true);
        variables.put("notes", "Task completed");

        doNothing().when(workflowService).completeTask(eq(taskId), any());

        // When
        ResponseEntity<ApiResponse<Void>> response = loanOriginationController.completeTask(taskId, variables);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Task completed successfully", response.getBody().getMessage());
        verify(workflowService).completeTask(taskId, variables);
    }

    @Test
    void getPendingTasks_Success() {
        // Given
        when(workflowService.getPendingTasks("system"))
                .thenReturn(mockTasks);

        // When
        ResponseEntity<List<TaskInfo>> response = loanOriginationController.getLoanOriginationTasks();

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(workflowService).getPendingTasks("system");
    }

    @Test
    void terminateProcess_Success() {
        // Given
        String processInstanceId = "process-123";

        doNothing().when(workflowService).terminateProcess(processInstanceId, "Manual termination via API");

        // When
        ResponseEntity<ApiResponse<Void>> response = loanOriginationController.terminateProcess(processInstanceId);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Process terminated successfully", response.getBody().getMessage());
        verify(workflowService).terminateProcess(processInstanceId, "Manual termination via API");
    }
}
