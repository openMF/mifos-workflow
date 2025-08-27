package org.mifos.workflow.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mifos.workflow.core.model.ProcessInstance;
import org.mifos.workflow.core.model.ProcessStatus;
import org.mifos.workflow.core.model.ProcessHistoryInfo;
import org.mifos.workflow.core.model.TaskInfo;
import org.mifos.workflow.dto.fineract.loan.LoanDisbursementRequestDTO;
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
class LoanDisbursementControllerTest {

    @Mock
    private WorkflowService workflowService;

    @InjectMocks
    private LoanDisbursementController loanDisbursementController;

    private LoanDisbursementRequestDTO validDisbursementRequest;
    private ProcessInstance mockProcessInstance;
    private List<TaskInfo> mockTasks;
    private ProcessStatus mockProcessStatus;

    @BeforeEach
    void setUp() {
        validDisbursementRequest = new LoanDisbursementRequestDTO();
        validDisbursementRequest.setLoanId(1L);
        validDisbursementRequest.setExternalId("DISB-001");
        validDisbursementRequest.setActualDisbursementDate(LocalDate.parse("2024-01-15"));
        validDisbursementRequest.setTransactionAmount(new BigDecimal("10000.00"));
        validDisbursementRequest.setDisbursementMethod("BANK_TRANSFER");
        validDisbursementRequest.setAccountNumber("1234567890");
        validDisbursementRequest.setNote("Loan disbursement");
        validDisbursementRequest.setRequestNotes("Approved for disbursement");
        validDisbursementRequest.setDisbursementOfficer("officer1");
        validDisbursementRequest.setManager("manager1");
        validDisbursementRequest.setItSupport("itsupport1");
        validDisbursementRequest.setComplianceCheck(false);
        validDisbursementRequest.setRequiresManagerApproval(true);
        validDisbursementRequest.setRequiresComplianceReview(false);
        validDisbursementRequest.setClientNotificationMethod("SMS");
        validDisbursementRequest.setRequireClientAcknowledgement(false);
        validDisbursementRequest.setPriority("NORMAL");
        validDisbursementRequest.setRiskLevel("LOW");
        validDisbursementRequest.setIsUrgent(false);
        validDisbursementRequest.setDisbursementChannel("BANK");
        validDisbursementRequest.setBankCode("BANK001");
        validDisbursementRequest.setBranchCode("BRANCH001");
        validDisbursementRequest.setReferenceNumber("REF001");
        validDisbursementRequest.setBeneficiaryName("John Doe");
        validDisbursementRequest.setBeneficiaryId("BEN001");
        validDisbursementRequest.setBeneficiaryPhone("+1234567890");
        validDisbursementRequest.setBeneficiaryEmail("john.doe@example.com");
        validDisbursementRequest.setDestinationAccount("9876543210");
        validDisbursementRequest.setDestinationBank("DESTBANK");
        validDisbursementRequest.setDestinationBranch("DESTBRANCH");
        validDisbursementRequest.setCurrencyCode("USD");
        validDisbursementRequest.setExchangeRate(new BigDecimal("1.00"));
        validDisbursementRequest.setSourceOfFunds("LOAN");
        validDisbursementRequest.setPurpose("Business expansion");
        validDisbursementRequest.setAutoRetryOnFailure(true);
        validDisbursementRequest.setMaxRetryAttempts(3);
        validDisbursementRequest.setEscalationLevel("LEVEL1");
        validDisbursementRequest.setCorrelationId("CORR001");
        validDisbursementRequest.setExpectedCompletionDate(LocalDate.parse("2024-01-16"));
        validDisbursementRequest.setCreatedBy("system");
        validDisbursementRequest.setCreatedDate(LocalDate.parse("2024-01-14"));

        mockProcessInstance = ProcessInstance.builder()
                .id("process-123")
                .processDefinitionId("loan-disbursement")
                .status("ACTIVE")
                .build();

        mockTasks = Arrays.asList(
                createTaskInfo("task-1", "Review Disbursement", "process-123"),
                createTaskInfo("task-2", "Approve Disbursement", "process-123"),
                createTaskInfo("task-3", "Execute Disbursement", "process-123")
        );

        mockProcessStatus = new ProcessStatus();
        mockProcessStatus.setProcessInstanceId("process-123");
        mockProcessStatus.setStatus("ACTIVE");
        mockProcessStatus.setCurrentActivityName("Review Disbursement");
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
    void startLoanDisbursement_Success() {
        // Given
        when(workflowService.startProcess(eq("loan-disbursement"), any()))
                .thenReturn(mockProcessInstance);

        // When
        ResponseEntity<ProcessInstance> response = loanDisbursementController.startLoanDisbursement(validDisbursementRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("process-123", response.getBody().getId());
        verify(workflowService).startProcess(eq("loan-disbursement"), any());
    }

    @Test
    void startLoanDisbursement_WithDisbursementData_Success() {
        // Given
        Map<String, Object> disbursementData = new HashMap<>();
        disbursementData.put("disbursementDate", LocalDate.parse("2024-01-15"));
        disbursementData.put("amount", new BigDecimal("10000.00"));
        disbursementData.put("method", "BANK_TRANSFER");
        validDisbursementRequest.setDisbursementData(Arrays.asList(disbursementData));

        when(workflowService.startProcess(eq("loan-disbursement"), any()))
                .thenReturn(mockProcessInstance);

        // When
        ResponseEntity<ProcessInstance> response = loanDisbursementController.startLoanDisbursement(validDisbursementRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(workflowService).startProcess(eq("loan-disbursement"), any());
    }

    @Test
    void startLoanDisbursement_WithAdditionalProperties_Success() {
        // Given
        Map<String, Object> additionalProps = new HashMap<>();
        additionalProps.put("customField", "customValue");
        additionalProps.put("priority", "HIGH");
        validDisbursementRequest.setAdditionalProperties(additionalProps);

        when(workflowService.startProcess(eq("loan-disbursement"), any()))
                .thenReturn(mockProcessInstance);

        // When
        ResponseEntity<ProcessInstance> response = loanDisbursementController.startLoanDisbursement(validDisbursementRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(workflowService).startProcess(eq("loan-disbursement"), any());
    }

    @Test
    void startLoanDisbursement_WithNullOptionalFields_Success() {
        // Given
        validDisbursementRequest.setDisbursementOfficer(null);
        validDisbursementRequest.setManager(null);
        validDisbursementRequest.setItSupport(null);
        validDisbursementRequest.setComplianceCheck(null);
        validDisbursementRequest.setRequiresManagerApproval(null);
        validDisbursementRequest.setRequiresComplianceReview(null);
        validDisbursementRequest.setRequireClientAcknowledgement(null);
        validDisbursementRequest.setPriority(null);
        validDisbursementRequest.setRiskLevel(null);
        validDisbursementRequest.setIsUrgent(null);
        validDisbursementRequest.setAutoRetryOnFailure(null);
        validDisbursementRequest.setMaxRetryAttempts(null);
        validDisbursementRequest.setEscalationLevel(null);
        validDisbursementRequest.setCreatedBy(null);
        validDisbursementRequest.setCreatedDate(null);

        when(workflowService.startProcess(eq("loan-disbursement"), any()))
                .thenReturn(mockProcessInstance);

        // When
        ResponseEntity<ProcessInstance> response = loanDisbursementController.startLoanDisbursement(validDisbursementRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(workflowService).startProcess(eq("loan-disbursement"), any());
    }

    @Test
    void getProcessTasks_Success() {
        // Given
        String processInstanceId = "process-123";
        when(workflowService.getPendingTasksForProcess(processInstanceId))
                .thenReturn(mockTasks);

        // When
        ResponseEntity<List<TaskInfo>> response = loanDisbursementController.getTasksForProcess(processInstanceId);

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
        ResponseEntity<ProcessStatus> response = loanDisbursementController.getProcessStatus(processInstanceId);

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

        // When
        ResponseEntity<ApiResponse<Void>> response = loanDisbursementController.completeTask(taskId, variables);

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

        // When
        ResponseEntity<ApiResponse<Void>> response = loanDisbursementController.completeTask(taskId, variables);

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
        String assignee = "system";
        when(workflowService.getPendingTasks(assignee))
                .thenReturn(mockTasks);

        // When
        ResponseEntity<List<TaskInfo>> response = loanDisbursementController.getTasksByRole(assignee);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().size());
        verify(workflowService).getPendingTasks(assignee);
    }

    @Test
    void getProcessHistory_Success() {
        // Given
        String processInstanceId = "process-123";
        List<ProcessHistoryInfo> mockHistory = Arrays.asList(
                ProcessHistoryInfo.builder().build(),
                ProcessHistoryInfo.builder().build(),
                ProcessHistoryInfo.builder().build()
        );

        when(workflowService.getProcessHistoryInfo())
                .thenReturn(mockHistory);

        // When
        ResponseEntity<List<ProcessHistoryInfo>> response = loanDisbursementController.getProcessHistory(processInstanceId);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().size());
        verify(workflowService).getProcessHistoryInfo();
    }

    @Test
    void cancelProcess_Success() {
        // Given
        String processInstanceId = "process-123";

        // When
        ResponseEntity<ApiResponse<Void>> response = loanDisbursementController.terminateProcess(processInstanceId);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Process terminated successfully", response.getBody().getMessage());
        verify(workflowService).terminateProcess(processInstanceId, "Manual termination via API");
    }

    @Test
    void startLoanDisbursement_WithUrgentPriority_Success() {
        // Given
        validDisbursementRequest.setIsUrgent(true);
        validDisbursementRequest.setUrgencyReason("Emergency funding required");
        validDisbursementRequest.setPriority("HIGH");

        when(workflowService.startProcess(eq("loan-disbursement"), any()))
                .thenReturn(mockProcessInstance);

        // When
        ResponseEntity<ProcessInstance> response = loanDisbursementController.startLoanDisbursement(validDisbursementRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(workflowService).startProcess(eq("loan-disbursement"), any());
    }

    @Test
    void startLoanDisbursement_WithComplianceReview_Success() {
        // Given
        validDisbursementRequest.setRequiresComplianceReview(true);
        validDisbursementRequest.setComplianceCheck(true);
        validDisbursementRequest.setComplianceNotes("Requires compliance review");

        when(workflowService.startProcess(eq("loan-disbursement"), any()))
                .thenReturn(mockProcessInstance);

        // When
        ResponseEntity<ProcessInstance> response = loanDisbursementController.startLoanDisbursement(validDisbursementRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(workflowService).startProcess(eq("loan-disbursement"), any());
    }

    @Test
    void startLoanDisbursement_WithClientAcknowledgement_Success() {
        // Given
        validDisbursementRequest.setRequireClientAcknowledgement(true);
        validDisbursementRequest.setClientNotificationMethod("EMAIL");

        when(workflowService.startProcess(eq("loan-disbursement"), any()))
                .thenReturn(mockProcessInstance);

        // When
        ResponseEntity<ProcessInstance> response = loanDisbursementController.startLoanDisbursement(validDisbursementRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(workflowService).startProcess(eq("loan-disbursement"), any());
    }

    @Test
    void startLoanDisbursement_WithRetryConfiguration_Success() {
        // Given
        validDisbursementRequest.setAutoRetryOnFailure(true);
        validDisbursementRequest.setMaxRetryAttempts(5);
        validDisbursementRequest.setEscalationLevel("LEVEL2");

        when(workflowService.startProcess(eq("loan-disbursement"), any()))
                .thenReturn(mockProcessInstance);

        // When
        ResponseEntity<ProcessInstance> response = loanDisbursementController.startLoanDisbursement(validDisbursementRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(workflowService).startProcess(eq("loan-disbursement"), any());
    }

    @Test
    void startLoanDisbursement_WithInternationalTransfer_Success() {
        // Given
        validDisbursementRequest.setCurrencyCode("EUR");
        validDisbursementRequest.setExchangeRate(new BigDecimal("0.85"));
        validDisbursementRequest.setDestinationBank("INTERNATIONAL_BANK");
        validDisbursementRequest.setDestinationBranch("INTERNATIONAL_BRANCH");

        when(workflowService.startProcess(eq("loan-disbursement"), any()))
                .thenReturn(mockProcessInstance);

        // When
        ResponseEntity<ProcessInstance> response = loanDisbursementController.startLoanDisbursement(validDisbursementRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(workflowService).startProcess(eq("loan-disbursement"), any());
    }
}
