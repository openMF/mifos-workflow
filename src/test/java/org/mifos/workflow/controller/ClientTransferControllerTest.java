package org.mifos.workflow.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mifos.workflow.core.model.ActiveProcess;
import org.mifos.workflow.core.model.DeploymentInfo;
import org.mifos.workflow.core.model.DeploymentInfoEnhanced;
import org.mifos.workflow.core.model.DeploymentResource;
import org.mifos.workflow.core.model.DeploymentResult;
import org.mifos.workflow.core.model.ProcessCompletionStatus;
import org.mifos.workflow.core.model.ProcessDefinitionInfo;
import org.mifos.workflow.core.model.ProcessHistoryInfo;
import org.mifos.workflow.core.model.ProcessInstance;
import org.mifos.workflow.core.model.ProcessStatus;
import org.mifos.workflow.core.model.ProcessVariables;
import org.mifos.workflow.core.model.TaskInfo;
import org.mifos.workflow.dto.fineract.client.ClientTransferRequestDTO;
import org.mifos.workflow.service.WorkflowService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.mifos.workflow.util.ApiResponse;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.*;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientTransferControllerTest {

    @Mock
    private WorkflowService workflowService;

    @InjectMocks
    private ClientTransferController clientTransferController;

    private ClientTransferRequestDTO transferRequest;
    private ProcessInstance processInstance;
    private List<TaskInfo> taskInfos;
    private Map<String, Object> taskVariables;

    @BeforeEach
    void setUp() {
        transferRequest = ClientTransferRequestDTO.builder().clientId(123L).destinationOfficeId(456L).transferDate(LocalDate.of(2024, 1, 1)).dateFormat("yyyy-MM-dd").locale("en").build();

        processInstance = ProcessInstance.builder().id("process-123").processDefinitionId("client-transfer").build();

        taskInfos = Arrays.asList(TaskInfo.builder().taskId("task-1").name("Transfer Approval Task").build(), TaskInfo.builder().taskId("task-2").name("Transfer Execution Task").build());

        taskVariables = new HashMap<>();
        taskVariables.put("approved", true);
    }

    @Test
    void startClientTransfer_Success() {
        // Given
        when(workflowService.startProcess(eq("client-transfer"), any(Map.class))).thenReturn(processInstance);

        // When
        ResponseEntity<ProcessInstance> response = clientTransferController.startClientTransfer(transferRequest);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("process-123", response.getBody().getId());
        verify(workflowService).startProcess(eq("client-transfer"), any(Map.class));
    }

    @Test
    void startClientTransfer_WithNullDateFormatAndLocale_UsesDefaults() {
        // Given
        ClientTransferRequestDTO requestWithNulls = ClientTransferRequestDTO.builder().clientId(123L).destinationOfficeId(456L).transferDate(LocalDate.of(2024, 1, 1)).build();

        when(workflowService.startProcess(eq("client-transfer"), any(Map.class))).thenReturn(processInstance);

        // When
        ResponseEntity<ProcessInstance> response = clientTransferController.startClientTransfer(requestWithNulls);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        verify(workflowService).startProcess(eq("client-transfer"), any(Map.class));
    }

    @Test
    void getClientTransferTasks_Success() {
        // Given
        when(workflowService.getPendingTasks("system")).thenReturn(taskInfos);

        // When
        ResponseEntity<List<TaskInfo>> response = clientTransferController.getClientTransferTasks();

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(workflowService).getPendingTasks("system");
    }

    @Test
    void completeTransferTask_Success() {
        // Given
        doNothing().when(workflowService).completeTask(anyString(), any(Map.class));

        // When
        ResponseEntity<ApiResponse<Void>> response = clientTransferController.completeTransferTask("task-123", taskVariables);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        verify(workflowService).completeTask("task-123", taskVariables);
    }

    @Test
    void getProcessVariables_Success() {
        // Given
        Map<String, Object> variables = new HashMap<>();
        variables.put("clientId", 123L);
        variables.put("destinationOfficeId", 456L);

        ProcessVariables processVariables = ProcessVariables.builder().variables(variables).build();

        when(workflowService.getProcessVariables("process-123")).thenReturn(processVariables);

        // When
        ResponseEntity<Map<String, Object>> response = clientTransferController.getProcessVariables("process-123");

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(123L, response.getBody().get("clientId"));
        assertEquals(456L, response.getBody().get("destinationOfficeId"));
        verify(workflowService).getProcessVariables("process-123");
    }

    @Test
    void getActiveProcesses_Success() {
        // Given
        List<ActiveProcess> activeProcesses = Arrays.asList(ActiveProcess.builder().processInstanceId("process-1").processDefinitionKey("client-transfer").build(), ActiveProcess.builder().processInstanceId("process-2").processDefinitionKey("client-transfer").build());
        when(workflowService.getActiveProcesses()).thenReturn(activeProcesses);

        // When
        ResponseEntity<List<ActiveProcess>> response = clientTransferController.getActiveProcesses();

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(workflowService).getActiveProcesses();
    }

    @Test
    void getProcessDefinitions_Success() {
        // Given
        List<ProcessDefinitionInfo> processDefinitions = Arrays.asList(ProcessDefinitionInfo.builder().processDefinitionId("def-1").processDefinitionKey("client-transfer").build(), ProcessDefinitionInfo.builder().processDefinitionId("def-2").processDefinitionKey("client-offboarding").build());
        when(workflowService.getProcessDefinitionsInfo()).thenReturn(processDefinitions);

        // When
        ResponseEntity<List<ProcessDefinitionInfo>> response = clientTransferController.getProcessDefinitions();

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(workflowService).getProcessDefinitionsInfo();
    }

    @Test
    void getProcessHistory_Success() {
        // Given
        List<ProcessHistoryInfo> processHistory = Arrays.asList(ProcessHistoryInfo.builder().historicProcessInstanceId("hist-1").processInstanceId("process-1").build(), ProcessHistoryInfo.builder().historicProcessInstanceId("hist-2").processInstanceId("process-2").build());
        when(workflowService.getProcessHistoryInfo()).thenReturn(processHistory);

        // When
        ResponseEntity<List<ProcessHistoryInfo>> response = clientTransferController.getProcessHistory();

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(workflowService).getProcessHistoryInfo();
    }

    @Test
    void getProcessHistory_Exception_ThrowsException() {
        // Given
        when(workflowService.getProcessHistoryInfo()).thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            clientTransferController.getProcessHistory();
        });
        verify(workflowService).getProcessHistoryInfo();
    }

    @Test
    void getHistoricProcessVariables_Success() {
        // Given
        Map<String, Object> variables = new HashMap<>();
        variables.put("completed", true);

        ProcessVariables processVariables = ProcessVariables.builder().variables(variables).build();

        when(workflowService.getHistoricProcessVariables("process-123")).thenReturn(processVariables);

        // When
        ResponseEntity<Map<String, Object>> response = clientTransferController.getHistoricProcessVariables("process-123");

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertTrue((Boolean) response.getBody().get("completed"));
        verify(workflowService).getHistoricProcessVariables("process-123");
    }

    @Test
    void getTaskVariables_Success() {
        // Given
        Map<String, Object> variables = new HashMap<>();
        variables.put("taskStatus", "pending");

        ProcessVariables processVariables = ProcessVariables.builder().variables(variables).build();

        when(workflowService.getTaskVariables("task-123")).thenReturn(processVariables);

        // When
        ResponseEntity<Map<String, Object>> response = clientTransferController.getTaskVariables("task-123");

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("pending", response.getBody().get("taskStatus"));
        verify(workflowService).getTaskVariables("task-123");
    }

    @Test
    void getTasksForProcess_Success() {
        // Given
        when(workflowService.getPendingTasksForProcess("process-123")).thenReturn(taskInfos);

        // When
        ResponseEntity<List<TaskInfo>> response = clientTransferController.getTasksForProcess("process-123");

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(workflowService).getPendingTasksForProcess("process-123");
    }

    @Test
    void terminateProcess_Success() {
        // Given
        doNothing().when(workflowService).terminateProcess(anyString(), anyString());

        // When
        ResponseEntity<ApiResponse<Void>> response = clientTransferController.terminateProcess("process-123");

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        verify(workflowService).terminateProcess("process-123", "Manual termination via API");
    }

    @Test
    void getProcessStatus_Success() {
        // Given
        ProcessStatus processStatus = ProcessStatus.builder().processInstanceId("process-123").processDefinitionKey("client-transfer").status("ACTIVE").build();
        when(workflowService.getProcessStatus("process-123")).thenReturn(processStatus);

        // When
        ResponseEntity<ProcessStatus> response = clientTransferController.getProcessStatus("process-123");

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("ACTIVE", response.getBody().getStatus());
        verify(workflowService).getProcessStatus("process-123");
    }

    @Test
    void getProcessCompletionStatus_Success() {
        // Given
        ProcessCompletionStatus processCompletionStatus = ProcessCompletionStatus.builder().processInstanceId("process-123").processDefinitionKey("client-transfer").outcome("COMPLETED").successful(true).build();
        when(workflowService.getProcessCompletionStatus("process-123")).thenReturn(processCompletionStatus);

        // When
        ResponseEntity<ProcessCompletionStatus> response = clientTransferController.getProcessCompletionStatus("process-123");

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getSuccessful());
        verify(workflowService).getProcessCompletionStatus("process-123");
    }

    @Test
    void getDeployments_Success() {
        // Given
        List<DeploymentInfo> deployments = Arrays.asList(DeploymentInfo.builder().id("deploy-1").name("Client Transfer").build(), DeploymentInfo.builder().id("deploy-2").name("Client Onboarding").build());
        when(workflowService.getDeployments()).thenReturn(deployments);

        // When
        ResponseEntity<List<DeploymentInfo>> response = clientTransferController.getDeployments();

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(workflowService).getDeployments();
    }

    @Test
    void getDeployment_Success() {
        // Given
        DeploymentInfoEnhanced deploymentInfo = DeploymentInfoEnhanced.builder().id("deploy-123").name("Client Transfer").build();
        when(workflowService.getDeploymentInfo("deploy-123")).thenReturn(deploymentInfo);

        // When
        ResponseEntity<DeploymentInfoEnhanced> response = clientTransferController.getDeployment("deploy-123");

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("deploy-123", response.getBody().getId());
        verify(workflowService).getDeploymentInfo("deploy-123");
    }

    @Test
    void getDeploymentResources_Success() {
        // Given
        List<DeploymentResource> resources = Arrays.asList(DeploymentResource.builder().id("resource-1").name("process.bpmn").build(), DeploymentResource.builder().id("resource-2").name("process.png").build());
        when(workflowService.getDeploymentResources("deploy-123")).thenReturn(resources);

        // When
        ResponseEntity<List<DeploymentResource>> response = clientTransferController.getDeploymentResources("deploy-123");

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(workflowService).getDeploymentResources("deploy-123");
    }

    @Test
    void getDeploymentResource_Success() {
        // Given
        byte[] resourceBytes = "process content".getBytes();
        when(workflowService.getDeploymentResource("deploy-123", "process.bpmn")).thenReturn(resourceBytes);

        // When
        ResponseEntity<byte[]> response = clientTransferController.getDeploymentResource("deploy-123", "process.bpmn");

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertArrayEquals(resourceBytes, response.getBody());
        verify(workflowService).getDeploymentResource("deploy-123", "process.bpmn");
    }

    @Test
    void deleteDeployment_Success() {
        // Given
        doNothing().when(workflowService).deleteDeployment("deploy-123");

        // When
        ResponseEntity<ApiResponse<Void>> response = clientTransferController.deleteDeployment("deploy-123");

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        verify(workflowService).deleteDeployment("deploy-123");
    }

    @Test
    void deployProcess_Success() throws IOException {
        // Given
        MockMultipartFile file = new MockMultipartFile("file", "process.bpmn", "application/xml", "process content".getBytes());

        DeploymentResult deploymentResult = DeploymentResult.builder().deploymentId("deploy-123").name("test-process.bpmn").success(true).build();

        when(workflowService.deployProcess(any(java.io.InputStream.class), eq("process.bpmn"))).thenReturn(deploymentResult);

        // When
        ResponseEntity<DeploymentResult> response = clientTransferController.deployProcess(file);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("deploy-123", response.getBody().getDeploymentId());
        verify(workflowService).deployProcess(any(java.io.InputStream.class), eq("process.bpmn"));
    }

    @Test
    void deployProcess_IOException_ThrowsRuntimeException() throws IOException {
        // Given
        MockMultipartFile file = new MockMultipartFile("file", "process.bpmn", "application/xml", "process content".getBytes());

        when(workflowService.deployProcess(any(java.io.InputStream.class), eq("process.bpmn"))).thenThrow(new RuntimeException("File read error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            clientTransferController.deployProcess(file);
        });
    }
}