package org.mifos.workflow.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mifos.workflow.config.WorkflowConfig;
import org.mifos.workflow.core.engine.WorkflowEngine;
import org.mifos.workflow.core.engine.WorkflowEngineFactory;
import org.mifos.workflow.core.engine.enums.EngineType;
import org.mifos.workflow.service.fineract.auth.FineractAuthService;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test class for WorkflowService.
 * Tests the basic functionality of the workflow service.
 */
@ExtendWith(MockitoExtension.class)
class WorkflowServiceTest {

    @Mock
    private WorkflowEngineFactory workflowEngineFactory;

    @Mock
    private FineractAuthService fineractAuthService;

    @Mock
    private WorkflowEngine workflowEngine;

    @Mock
    private WorkflowConfig workflowConfig;

    @Test
    void testWorkflowServiceInitialization() {
        // Given
        when(workflowEngineFactory.getWorkflowEngine()).thenReturn(workflowEngine);
        when(workflowEngine.getEngineType()).thenReturn(EngineType.FLOWABLE);
        when(workflowEngine.isEngineActive()).thenReturn(true);

        // When
        WorkflowService workflowService = new WorkflowService(workflowEngineFactory, fineractAuthService, workflowConfig);

        // Then
        assertNotNull(workflowService);
        assertEquals("FLOWABLE", workflowService.getEngineType());
        assertTrue(workflowService.isEngineActive());
    }

    @Test
    void testGetWorkflowEngine() {
        // Given
        when(workflowEngineFactory.getWorkflowEngine()).thenReturn(workflowEngine);
        WorkflowService workflowService = new WorkflowService(workflowEngineFactory, fineractAuthService, workflowConfig);

        // When
        WorkflowEngine result = workflowService.getWorkflowEngine();

        // Then
        assertNotNull(result);
        verify(workflowEngineFactory).getWorkflowEngine();
    }

    @Test
    void testGetEngineType() {
        // Given
        when(workflowEngineFactory.getWorkflowEngine()).thenReturn(workflowEngine);
        when(workflowEngine.getEngineType()).thenReturn(EngineType.FLOWABLE);
        WorkflowService workflowService = new WorkflowService(workflowEngineFactory, fineractAuthService, workflowConfig);

        // When
        String engineType = workflowService.getEngineType();

        // Then
        assertEquals("FLOWABLE", engineType);
    }

    @Test
    void testIsEngineActive() {
        // Given
        when(workflowEngineFactory.getWorkflowEngine()).thenReturn(workflowEngine);
        when(workflowEngine.isEngineActive()).thenReturn(true);
        WorkflowService workflowService = new WorkflowService(workflowEngineFactory, fineractAuthService, workflowConfig);

        // When
        boolean isActive = workflowService.isEngineActive();

        // Then
        assertTrue(isActive);
    }

    @Test
    void testGetWorkflowConfig() {
        // Given
        WorkflowService workflowService = new WorkflowService(workflowEngineFactory, fineractAuthService, workflowConfig);

        // When
        WorkflowConfig result = workflowService.getWorkflowConfig();

        // Then
        assertNotNull(result);
        assertEquals(workflowConfig, result);
    }
} 