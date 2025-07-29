package org.mifos.workflow.core.engine;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mifos.workflow.config.WorkflowConfig;
import org.mifos.workflow.engine.flowable.FlowableMapper;
import org.mifos.workflow.engine.flowable.FlowableWorkflowEngine;
import org.mifos.workflow.core.engine.enums.EngineType;
import org.mifos.workflow.util.WorkflowErrorHandler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkflowEngineTest {

    @Mock
    private WorkflowConfig workflowConfig;

    @Mock
    private FlowableMapper flowableMapper;

    @Mock
    private org.flowable.engine.ProcessEngine processEngine;

    @Test
    void flowableWorkflowEngine_ImplementsWorkflowEngineInterface() {
        // Given
        when(processEngine.getRepositoryService()).thenReturn(mock(org.flowable.engine.RepositoryService.class));
        when(processEngine.getRuntimeService()).thenReturn(mock(org.flowable.engine.RuntimeService.class));
        when(processEngine.getTaskService()).thenReturn(mock(org.flowable.engine.TaskService.class));
        when(processEngine.getHistoryService()).thenReturn(mock(org.flowable.engine.HistoryService.class));

        // When
        FlowableWorkflowEngine flowableEngine = new FlowableWorkflowEngine(workflowConfig, processEngine, flowableMapper);

        // Then
        assertTrue(flowableEngine instanceof WorkflowEngine, "FlowableWorkflowEngine should implement WorkflowEngine interface");
    }

    @Test
    void flowableWorkflowEngine_ReturnsCorrectEngineType() {
        // Given
        when(processEngine.getRepositoryService()).thenReturn(mock(org.flowable.engine.RepositoryService.class));
        when(processEngine.getRuntimeService()).thenReturn(mock(org.flowable.engine.RuntimeService.class));
        when(processEngine.getTaskService()).thenReturn(mock(org.flowable.engine.TaskService.class));
        when(processEngine.getHistoryService()).thenReturn(mock(org.flowable.engine.HistoryService.class));

        FlowableWorkflowEngine flowableEngine = new FlowableWorkflowEngine(workflowConfig, processEngine, flowableMapper);

        // When
        EngineType engineType = flowableEngine.getEngineType();

        // Then
        assertEquals(EngineType.FLOWABLE, engineType, "FlowableWorkflowEngine should return FLOWABLE engine type");
    }

    @Test
    void workflowEngineInterface_DefinesAllRequiredMethods() {
        // This test verifies that the WorkflowEngine interface has all the expected methods
        // by checking that FlowableWorkflowEngine implements them

        // Setup Flowable service mocks
        org.flowable.engine.RepositoryService repositoryService = mock(org.flowable.engine.RepositoryService.class);
        org.flowable.engine.RuntimeService runtimeService = mock(org.flowable.engine.RuntimeService.class);
        org.flowable.engine.TaskService taskService = mock(org.flowable.engine.TaskService.class);
        org.flowable.engine.HistoryService historyService = mock(org.flowable.engine.HistoryService.class);

        when(processEngine.getRepositoryService()).thenReturn(repositoryService);
        when(processEngine.getRuntimeService()).thenReturn(runtimeService);
        when(processEngine.getTaskService()).thenReturn(taskService);
        when(processEngine.getHistoryService()).thenReturn(historyService);

        // Setup query mocks
        org.flowable.engine.repository.ProcessDefinitionQuery processDefinitionQuery = mock(org.flowable.engine.repository.ProcessDefinitionQuery.class);
        org.flowable.engine.runtime.ProcessInstanceQuery processInstanceQuery = mock(org.flowable.engine.runtime.ProcessInstanceQuery.class);
        org.flowable.task.api.TaskQuery taskQuery = mock(org.flowable.task.api.TaskQuery.class);
        org.flowable.engine.history.HistoricProcessInstanceQuery historicProcessInstanceQuery = mock(org.flowable.engine.history.HistoricProcessInstanceQuery.class);
        org.flowable.engine.repository.DeploymentQuery deploymentQuery = mock(org.flowable.engine.repository.DeploymentQuery.class);

        when(repositoryService.createProcessDefinitionQuery()).thenReturn(processDefinitionQuery);
        when(repositoryService.createDeploymentQuery()).thenReturn(deploymentQuery);
        when(runtimeService.createProcessInstanceQuery()).thenReturn(processInstanceQuery);
        when(taskService.createTaskQuery()).thenReturn(taskQuery);
        when(historyService.createHistoricProcessInstanceQuery()).thenReturn(historicProcessInstanceQuery);

        // Setup query chains
        when(processDefinitionQuery.active()).thenReturn(processDefinitionQuery);
        when(processDefinitionQuery.list()).thenReturn(java.util.Collections.emptyList());

        when(deploymentQuery.orderByDeploymentTime()).thenReturn(deploymentQuery);
        when(deploymentQuery.desc()).thenReturn(deploymentQuery);
        when(deploymentQuery.list()).thenReturn(java.util.Collections.emptyList());

        when(processInstanceQuery.active()).thenReturn(processInstanceQuery);
        when(processInstanceQuery.processInstanceId(anyString())).thenReturn(processInstanceQuery);
        when(processInstanceQuery.singleResult()).thenReturn(null);
        when(processInstanceQuery.list()).thenReturn(java.util.Collections.emptyList());

        when(taskQuery.taskAssignee(anyString())).thenReturn(taskQuery);
        when(taskQuery.list()).thenReturn(java.util.Collections.emptyList());

        when(taskQuery.processInstanceId(anyString())).thenReturn(taskQuery);

        when(historicProcessInstanceQuery.finished()).thenReturn(historicProcessInstanceQuery);
        when(historicProcessInstanceQuery.orderByProcessInstanceEndTime()).thenReturn(historicProcessInstanceQuery);
        when(historicProcessInstanceQuery.desc()).thenReturn(historicProcessInstanceQuery);
        when(historicProcessInstanceQuery.list()).thenReturn(java.util.Collections.emptyList());

        FlowableWorkflowEngine flowableEngine = new FlowableWorkflowEngine(workflowConfig, processEngine, flowableMapper);

        // Mock WorkflowErrorHandler for all method calls
        MockedStatic<WorkflowErrorHandler> mockedWorkflowErrorHandler = mockStatic(WorkflowErrorHandler.class);
        mockedWorkflowErrorHandler.when(() -> WorkflowErrorHandler.executeWithExceptionHandling(anyString(), anyString(), any(java.util.function.Supplier.class))).thenAnswer(invocation -> {
            return invocation.getArgument(2, java.util.function.Supplier.class).get();
        });
        mockedWorkflowErrorHandler.when(() -> WorkflowErrorHandler.executeWithExceptionHandling(anyString(), anyString(), any(Runnable.class))).thenAnswer(invocation -> {
            invocation.getArgument(2, Runnable.class).run();
            return null;
        });

        try {
            // Verify that all interface methods are accessible
            assertDoesNotThrow(flowableEngine::getProcessDefinitions);
            assertDoesNotThrow(flowableEngine::getDeployments);
            assertDoesNotThrow(flowableEngine::getProcessInstances);
            assertDoesNotThrow(() -> flowableEngine.getPendingTasks("testUser"));
            assertDoesNotThrow(() -> flowableEngine.getPendingTasksForProcess("testProcess"));
            assertDoesNotThrow(flowableEngine::getHistoricProcesses);
            assertDoesNotThrow(flowableEngine::getEngineType);
            assertDoesNotThrow(flowableEngine::isEngineActive);
            assertDoesNotThrow(() -> flowableEngine.isProcessActive("testProcess"));
        } finally {
            mockedWorkflowErrorHandler.close();
        }
    }

    @Test
    void workflowEngineInterface_SupportsPolymorphism() {
        // Given
        when(processEngine.getRepositoryService()).thenReturn(mock(org.flowable.engine.RepositoryService.class));
        when(processEngine.getRuntimeService()).thenReturn(mock(org.flowable.engine.RuntimeService.class));
        when(processEngine.getTaskService()).thenReturn(mock(org.flowable.engine.TaskService.class));
        when(processEngine.getHistoryService()).thenReturn(mock(org.flowable.engine.HistoryService.class));

        // When - Using the interface type
        WorkflowEngine engine = new FlowableWorkflowEngine(workflowConfig, processEngine, flowableMapper);

        // Then
        assertNotNull(engine, "Should be able to assign FlowableWorkflowEngine to WorkflowEngine interface");
        assertEquals(EngineType.FLOWABLE, engine.getEngineType(), "Should be able to call interface methods");
    }
}