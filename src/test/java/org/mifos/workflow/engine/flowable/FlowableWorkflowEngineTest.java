package org.mifos.workflow.engine.flowable;

import org.flowable.engine.ProcessEngine;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.HistoryService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.DeploymentBuilder;
import org.flowable.engine.repository.ProcessDefinitionQuery;
import org.flowable.engine.runtime.ProcessInstanceQuery;

import org.flowable.task.api.TaskQuery;
import org.flowable.engine.history.HistoricProcessInstanceQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mifos.workflow.core.model.DeploymentResult;
import org.mifos.workflow.core.model.ProcessVariables;

import org.mifos.workflow.util.WorkflowErrorHandler;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mifos.workflow.config.WorkflowConfig;
import org.mifos.workflow.core.engine.enums.EngineType;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlowableWorkflowEngineTest {

    @Mock
    private WorkflowConfig workflowConfig;

    @Mock
    private FlowableMapper flowableMapper;

    @Mock
    private ProcessEngine processEngine;

    @Mock
    private RepositoryService repositoryService;

    @Mock
    private RuntimeService runtimeService;

    @Mock
    private TaskService taskService;

    @Mock
    private HistoryService historyService;

    @Mock
    private ProcessDefinitionQuery processDefinitionQuery;

    @Mock
    private ProcessInstanceQuery processInstanceQuery;

    @Mock
    private TaskQuery taskQuery;

    @Mock
    private HistoricProcessInstanceQuery historicProcessInstanceQuery;

    @Mock
    private DeploymentBuilder deploymentBuilder;

    @Mock
    private Deployment deployment;

    @Mock
    private org.flowable.engine.repository.ProcessDefinition flowableProcessDefinition;

    @Mock
    private org.flowable.engine.runtime.ProcessInstance flowableProcessInstance;

    @Mock
    private org.flowable.task.api.Task flowableTask;

    @Mock
    private org.flowable.engine.history.HistoricProcessInstance flowableHistoricProcessInstance;

    private FlowableWorkflowEngine flowableWorkflowEngine;

    @BeforeEach
    void setUp() {
        // Setup service mocks - only what's needed for constructor
        when(processEngine.getRepositoryService()).thenReturn(repositoryService);
        when(processEngine.getRuntimeService()).thenReturn(runtimeService);
        when(processEngine.getTaskService()).thenReturn(taskService);
        when(processEngine.getHistoryService()).thenReturn(historyService);

        flowableWorkflowEngine = new FlowableWorkflowEngine(workflowConfig, processEngine, flowableMapper);
    }

    @Test
    void getProcessDefinitions_Success() {
        // Given
        when(repositoryService.createProcessDefinitionQuery()).thenReturn(processDefinitionQuery);
        List<org.flowable.engine.repository.ProcessDefinition> flowableDefinitions = Arrays.asList(flowableProcessDefinition);
        when(processDefinitionQuery.active()).thenReturn(processDefinitionQuery);
        when(processDefinitionQuery.list()).thenReturn(flowableDefinitions);

        org.mifos.workflow.core.model.ProcessDefinition mappedDefinition = org.mifos.workflow.core.model.ProcessDefinition.builder().id("test-process").key("test-key").name("Test Process").version(1).build();
        when(flowableMapper.mapToProcessDefinition(flowableProcessDefinition)).thenReturn(mappedDefinition);

        // When
        MockedStatic<WorkflowErrorHandler> mockedWorkflowErrorHandler = mockStatic(WorkflowErrorHandler.class);
        mockedWorkflowErrorHandler.when(() -> WorkflowErrorHandler.executeWithExceptionHandling(anyString(), anyString(), any(java.util.function.Supplier.class))).thenAnswer(invocation -> {
            // Get the supplier from the invocation and execute it
            return invocation.getArgument(2, java.util.function.Supplier.class).get();
        });

        try {
            List<org.mifos.workflow.core.model.ProcessDefinition> result = flowableWorkflowEngine.getProcessDefinitions();

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("test-process", result.get(0).getId());
            verify(flowableMapper).mapToProcessDefinition(flowableProcessDefinition);
        } finally {
            mockedWorkflowErrorHandler.close();
        }
    }

    @Test
    void deployProcess_Success() {
        // Given
        String filename = "test-process.bpmn20.xml";
        InputStream processDefinition = new ByteArrayInputStream("<xml>test</xml>".getBytes());

        when(repositoryService.createDeployment()).thenReturn(deploymentBuilder);
        when(deploymentBuilder.addInputStream(filename, processDefinition)).thenReturn(deploymentBuilder);
        when(deploymentBuilder.name(filename)).thenReturn(deploymentBuilder);
        when(deploymentBuilder.deploy()).thenReturn(deployment);

        when(deployment.getId()).thenReturn("deployment-123");
        when(deployment.getName()).thenReturn(filename);
        when(deployment.getDeploymentTime()).thenReturn(java.util.Date.from(LocalDateTime.now().atZone(java.time.ZoneId.systemDefault()).toInstant()));

        // When
        MockedStatic<WorkflowErrorHandler> mockedWorkflowErrorHandler = mockStatic(WorkflowErrorHandler.class);
        mockedWorkflowErrorHandler.when(() -> WorkflowErrorHandler.executeWithExceptionHandling(anyString(), anyString(), any(java.util.function.Supplier.class))).thenAnswer(invocation -> {
            return invocation.getArgument(2, java.util.function.Supplier.class).get();
        });

        try {
            DeploymentResult result = flowableWorkflowEngine.deployProcess(processDefinition, filename);

            // Then
            assertNotNull(result);
            assertEquals("deployment-123", result.getDeploymentId());
            assertEquals(filename, result.getName());
            assertTrue(result.isSuccess());
            assertTrue(result.getErrors().isEmpty());
        } finally {
            mockedWorkflowErrorHandler.close();
        }
    }

    @Test
    void deleteDeployment_Success() {
        // Given
        String deploymentId = "deployment-123";

        // When
        MockedStatic<WorkflowErrorHandler> mockedWorkflowErrorHandler = mockStatic(WorkflowErrorHandler.class);
        mockedWorkflowErrorHandler.when(() -> WorkflowErrorHandler.executeWithExceptionHandling(anyString(), anyString(), any(Runnable.class))).thenAnswer(invocation -> {
            invocation.getArgument(2, Runnable.class).run();
            return null;
        });

        try {
            flowableWorkflowEngine.deleteDeployment(deploymentId);

            // Then
            verify(repositoryService).deleteDeployment(deploymentId, true);
        } finally {
            mockedWorkflowErrorHandler.close();
        }
    }

    @Test
    void startProcess_Success() {
        // Given
        String processDefinitionKey = "test-process";
        ProcessVariables variables = ProcessVariables.builder().variables(Map.of("clientId", "123", "amount", "1000")).build();

        when(runtimeService.startProcessInstanceByKey(eq(processDefinitionKey), anyMap())).thenReturn(flowableProcessInstance);

        org.mifos.workflow.core.model.ProcessInstance mappedInstance = org.mifos.workflow.core.model.ProcessInstance.builder().id("process-instance-123").processDefinitionId("process-def-123").businessKey("business-key-123").startTime(LocalDateTime.now()).build();
        when(flowableMapper.mapToProcessInstance(flowableProcessInstance)).thenReturn(mappedInstance);

        // When
        MockedStatic<WorkflowErrorHandler> mockedWorkflowErrorHandler = mockStatic(WorkflowErrorHandler.class);
        mockedWorkflowErrorHandler.when(() -> WorkflowErrorHandler.executeWithExceptionHandling(anyString(), anyString(), any(java.util.function.Supplier.class))).thenAnswer(invocation -> {
            return invocation.getArgument(2, java.util.function.Supplier.class).get();
        });

        try {
            org.mifos.workflow.core.model.ProcessInstance result = flowableWorkflowEngine.startProcess(processDefinitionKey, variables);

            // Then
            assertNotNull(result);
            assertEquals("process-instance-123", result.getId());
            assertEquals("process-def-123", result.getProcessDefinitionId());
            verify(flowableMapper).mapToProcessInstance(flowableProcessInstance);
        } finally {
            mockedWorkflowErrorHandler.close();
        }
    }

    @Test
    void getProcessInstances_Success() {
        // Given
        when(runtimeService.createProcessInstanceQuery()).thenReturn(processInstanceQuery);
        when(processInstanceQuery.active()).thenReturn(processInstanceQuery);
        List<org.flowable.engine.runtime.ProcessInstance> flowableInstances = Arrays.asList(flowableProcessInstance);
        when(processInstanceQuery.list()).thenReturn(flowableInstances);

        org.mifos.workflow.core.model.ProcessInstance mappedInstance = org.mifos.workflow.core.model.ProcessInstance.builder().id("process-instance-123").processDefinitionId("process-def-123").businessKey("business-key-123").startTime(LocalDateTime.now()).build();
        when(flowableMapper.mapToProcessInstance(flowableProcessInstance)).thenReturn(mappedInstance);

        // When
        MockedStatic<WorkflowErrorHandler> mockedWorkflowErrorHandler = mockStatic(WorkflowErrorHandler.class);
        mockedWorkflowErrorHandler.when(() -> WorkflowErrorHandler.executeWithExceptionHandling(anyString(), anyString(), any(java.util.function.Supplier.class))).thenAnswer(invocation -> {
            return invocation.getArgument(2, java.util.function.Supplier.class).get();
        });

        try {
            List<org.mifos.workflow.core.model.ProcessInstance> result = flowableWorkflowEngine.getProcessInstances();

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("process-instance-123", result.get(0).getId());
            verify(flowableMapper).mapToProcessInstance(flowableProcessInstance);
        } finally {
            mockedWorkflowErrorHandler.close();
        }
    }

    @Test
    void completeTask_Success() {
        // Given
        String taskId = "task-123";
        ProcessVariables variables = ProcessVariables.builder().variables(Map.of("approved", "true")).build();

        // When
        MockedStatic<WorkflowErrorHandler> mockedWorkflowErrorHandler = mockStatic(WorkflowErrorHandler.class);
        mockedWorkflowErrorHandler.when(() -> WorkflowErrorHandler.executeWithExceptionHandling(anyString(), anyString(), any(Runnable.class))).thenAnswer(invocation -> {
            invocation.getArgument(2, Runnable.class).run();
            return null;
        });

        try {
            flowableWorkflowEngine.completeTask(taskId, variables);

            // Then
            verify(taskService).complete(eq(taskId), anyMap());
        } finally {
            mockedWorkflowErrorHandler.close();
        }
    }

    @Test
    void getPendingTasks_Success() {
        // Given
        String userId = "user-123";
        when(taskService.createTaskQuery()).thenReturn(taskQuery);
        when(taskQuery.taskAssignee(userId)).thenReturn(taskQuery);
        List<org.flowable.task.api.Task> flowableTasks = Arrays.asList(flowableTask);
        when(taskQuery.list()).thenReturn(flowableTasks);

        org.mifos.workflow.core.model.TaskInfo mappedTask = org.mifos.workflow.core.model.TaskInfo.builder().taskId("task-123").name("Approve Loan").description("Review and approve loan application").assignee(userId).processId("process-instance-123").createTime(LocalDateTime.now()).build();
        when(flowableMapper.mapToTaskInfo(flowableTask)).thenReturn(mappedTask);

        // When
        MockedStatic<WorkflowErrorHandler> mockedWorkflowErrorHandler = mockStatic(WorkflowErrorHandler.class);
        mockedWorkflowErrorHandler.when(() -> WorkflowErrorHandler.executeWithExceptionHandling(anyString(), anyString(), any(java.util.function.Supplier.class))).thenAnswer(invocation -> {
            return invocation.getArgument(2, java.util.function.Supplier.class).get();
        });

        try {
            List<org.mifos.workflow.core.model.TaskInfo> result = flowableWorkflowEngine.getPendingTasks(userId);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("task-123", result.get(0).getTaskId());
            assertEquals(userId, result.get(0).getAssignee());
            verify(flowableMapper).mapToTaskInfo(flowableTask);
        } finally {
            mockedWorkflowErrorHandler.close();
        }
    }

    @Test
    void getPendingTasksForProcess_Success() {
        // Given
        String processInstanceId = "process-instance-123";
        when(taskService.createTaskQuery()).thenReturn(taskQuery);
        when(taskQuery.processInstanceId(processInstanceId)).thenReturn(taskQuery);
        List<org.flowable.task.api.Task> flowableTasks = Arrays.asList(flowableTask);
        when(taskQuery.list()).thenReturn(flowableTasks);

        org.mifos.workflow.core.model.TaskInfo mappedTask = org.mifos.workflow.core.model.TaskInfo.builder().taskId("task-123").name("Approve Loan").description("Review and approve loan application").assignee("user-123").processId(processInstanceId).createTime(LocalDateTime.now()).build();
        when(flowableMapper.mapToTaskInfo(flowableTask)).thenReturn(mappedTask);

        // When
        MockedStatic<WorkflowErrorHandler> mockedWorkflowErrorHandler = mockStatic(WorkflowErrorHandler.class);
        mockedWorkflowErrorHandler.when(() -> WorkflowErrorHandler.executeWithExceptionHandling(anyString(), anyString(), any(java.util.function.Supplier.class))).thenAnswer(invocation -> {
            return invocation.getArgument(2, java.util.function.Supplier.class).get();
        });

        try {
            List<org.mifos.workflow.core.model.TaskInfo> result = flowableWorkflowEngine.getPendingTasksForProcess(processInstanceId);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("task-123", result.get(0).getTaskId());
            assertEquals(processInstanceId, result.get(0).getProcessId());
            verify(flowableMapper).mapToTaskInfo(flowableTask);
        } finally {
            mockedWorkflowErrorHandler.close();
        }
    }

    @Test
    void getHistoricProcesses_Success() {
        // Given
        when(historyService.createHistoricProcessInstanceQuery()).thenReturn(historicProcessInstanceQuery);
        when(historicProcessInstanceQuery.finished()).thenReturn(historicProcessInstanceQuery);
        when(historicProcessInstanceQuery.orderByProcessInstanceEndTime()).thenReturn(historicProcessInstanceQuery);
        when(historicProcessInstanceQuery.desc()).thenReturn(historicProcessInstanceQuery);
        List<org.flowable.engine.history.HistoricProcessInstance> flowableHistoricInstances = Arrays.asList(flowableHistoricProcessInstance);
        when(historicProcessInstanceQuery.list()).thenReturn(flowableHistoricInstances);

        org.mifos.workflow.core.model.HistoricProcessInstance mappedHistoricInstance = org.mifos.workflow.core.model.HistoricProcessInstance.builder().id("process-instance-123").processDefinitionId("process-def-123").businessKey("business-key-123").startTime(LocalDateTime.now()).endTime(LocalDateTime.now().plusHours(1)).durationInMillis(3600000L).startUserId("user-123").build();
        when(flowableMapper.mapToHistoricProcessInstance(flowableHistoricProcessInstance)).thenReturn(mappedHistoricInstance);

        // When
        MockedStatic<WorkflowErrorHandler> mockedWorkflowErrorHandler = mockStatic(WorkflowErrorHandler.class);
        mockedWorkflowErrorHandler.when(() -> WorkflowErrorHandler.executeWithExceptionHandling(anyString(), anyString(), any(java.util.function.Supplier.class))).thenAnswer(invocation -> {
            return invocation.getArgument(2, java.util.function.Supplier.class).get();
        });

        try {
            List<org.mifos.workflow.core.model.HistoricProcessInstance> result = flowableWorkflowEngine.getHistoricProcesses();

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("process-instance-123", result.get(0).getId());
            verify(flowableMapper).mapToHistoricProcessInstance(flowableHistoricProcessInstance);
        } finally {
            mockedWorkflowErrorHandler.close();
        }
    }

    @Test
    void getHistoricProcessInstance_Success() {
        // Given
        String processInstanceId = "process-instance-123";
        when(historyService.createHistoricProcessInstanceQuery()).thenReturn(historicProcessInstanceQuery);
        when(historicProcessInstanceQuery.processInstanceId(processInstanceId)).thenReturn(historicProcessInstanceQuery);
        when(historicProcessInstanceQuery.singleResult()).thenReturn(flowableHistoricProcessInstance);

        org.mifos.workflow.core.model.HistoricProcessInstance mappedHistoricInstance = org.mifos.workflow.core.model.HistoricProcessInstance.builder().id(processInstanceId).processDefinitionId("process-def-123").businessKey("business-key-123").startTime(LocalDateTime.now()).endTime(LocalDateTime.now().plusHours(1)).durationInMillis(3600000L).startUserId("user-123").build();
        when(flowableMapper.mapToHistoricProcessInstance(flowableHistoricProcessInstance)).thenReturn(mappedHistoricInstance);

        // When
        MockedStatic<WorkflowErrorHandler> mockedWorkflowErrorHandler = mockStatic(WorkflowErrorHandler.class);
        mockedWorkflowErrorHandler.when(() -> WorkflowErrorHandler.executeWithExceptionHandling(anyString(), anyString(), any(java.util.function.Supplier.class))).thenAnswer(invocation -> {
            return invocation.getArgument(2, java.util.function.Supplier.class).get();
        });

        try {
            org.mifos.workflow.core.model.HistoricProcessInstance result = flowableWorkflowEngine.getHistoricProcessInstance(processInstanceId);

            // Then
            assertNotNull(result);
            assertEquals(processInstanceId, result.getId());
            verify(flowableMapper).mapToHistoricProcessInstance(flowableHistoricProcessInstance);
        } finally {
            mockedWorkflowErrorHandler.close();
        }
    }

    @Test
    void getEngineType_ReturnsFlowable() {
        // When
        EngineType result = flowableWorkflowEngine.getEngineType();

        // Then
        assertEquals(EngineType.FLOWABLE, result);
    }

    @Test
    void isEngineActive_ReturnsTrue() {
        // When
        boolean result = flowableWorkflowEngine.isEngineActive();

        // Then
        assertTrue(result);
    }

    @Test
    void isProcessActive_ReturnsTrue() {
        // Given
        String processInstanceId = "process-instance-123";
        when(runtimeService.createProcessInstanceQuery()).thenReturn(processInstanceQuery);
        when(processInstanceQuery.processInstanceId(processInstanceId)).thenReturn(processInstanceQuery);
        when(processInstanceQuery.singleResult()).thenReturn(flowableProcessInstance);

        // When
        MockedStatic<WorkflowErrorHandler> mockedWorkflowErrorHandler = mockStatic(WorkflowErrorHandler.class);
        mockedWorkflowErrorHandler.when(() -> WorkflowErrorHandler.executeWithExceptionHandling(anyString(), anyString(), any(java.util.function.Supplier.class))).thenAnswer(invocation -> {
            return invocation.getArgument(2, java.util.function.Supplier.class).get();
        });

        try {
            boolean result = flowableWorkflowEngine.isProcessActive(processInstanceId);

            // Then
            assertTrue(result);
        } finally {
            mockedWorkflowErrorHandler.close();
        }
    }

    @Test
    void isProcessActive_ReturnsFalse() {
        // Given
        String processInstanceId = "process-instance-123";
        when(runtimeService.createProcessInstanceQuery()).thenReturn(processInstanceQuery);
        when(processInstanceQuery.processInstanceId(processInstanceId)).thenReturn(processInstanceQuery);
        when(processInstanceQuery.singleResult()).thenReturn(null);

        // When
        MockedStatic<WorkflowErrorHandler> mockedWorkflowErrorHandler = mockStatic(WorkflowErrorHandler.class);
        mockedWorkflowErrorHandler.when(() -> WorkflowErrorHandler.executeWithExceptionHandling(anyString(), anyString(), any(java.util.function.Supplier.class))).thenAnswer(invocation -> {
            return invocation.getArgument(2, java.util.function.Supplier.class).get();
        });

        try {
            boolean result = flowableWorkflowEngine.isProcessActive(processInstanceId);

            // Then
            assertFalse(result);
        } finally {
            mockedWorkflowErrorHandler.close();
        }
    }

    @Test
    void terminateProcess_Success() {
        // Given
        String processInstanceId = "process-instance-123";
        String reason = "Business decision";

        // When
        MockedStatic<WorkflowErrorHandler> mockedWorkflowErrorHandler = mockStatic(WorkflowErrorHandler.class);
        mockedWorkflowErrorHandler.when(() -> WorkflowErrorHandler.executeWithExceptionHandling(anyString(), anyString(), any(Runnable.class))).thenAnswer(invocation -> {
            invocation.getArgument(2, Runnable.class).run();
            return null;
        });

        try {
            flowableWorkflowEngine.terminateProcess(processInstanceId, reason);

            // Then
            verify(runtimeService).deleteProcessInstance(processInstanceId, reason);
        } finally {
            mockedWorkflowErrorHandler.close();
        }
    }
}