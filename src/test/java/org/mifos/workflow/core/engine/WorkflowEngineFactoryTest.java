package org.mifos.workflow.core.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mifos.workflow.config.WorkflowConfig;
import org.mifos.workflow.engine.flowable.FlowableWorkflowEngine;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkflowEngineFactoryTest {

    @Mock
    private WorkflowConfig workflowConfig;

    @Mock
    private FlowableWorkflowEngine flowableWorkflowEngine;

    @Mock
    private WorkflowConfig.Engine engineConfig;

    private WorkflowEngineFactory workflowEngineFactory;

    @BeforeEach
    void setUp() {
        workflowEngineFactory = new WorkflowEngineFactory(workflowConfig, flowableWorkflowEngine);
    }

    @Test
    void init_WithFlowableEngine_SetsFlowableEngine() throws Exception {
        // Given
        when(workflowConfig.getEngine()).thenReturn(engineConfig);
        when(engineConfig.getType()).thenReturn("flowable");

        // When
        callInitMethod();

        // Then
        assertNotNull(workflowEngineFactory.getWorkflowEngine());
        assertEquals(flowableWorkflowEngine, workflowEngineFactory.getWorkflowEngine());
    }

    @Test
    void init_WithTemporalEngine_FallsBackToFlowable() throws Exception {
        // Given
        when(workflowConfig.getEngine()).thenReturn(engineConfig);
        when(engineConfig.getType()).thenReturn("temporal");

        // When
        callInitMethod();

        // Then
        assertNotNull(workflowEngineFactory.getWorkflowEngine());
        assertEquals(flowableWorkflowEngine, workflowEngineFactory.getWorkflowEngine());
    }

    @Test
    void init_WithUnsupportedEngine_ThrowsException() throws Exception {
        // Given
        when(workflowConfig.getEngine()).thenReturn(engineConfig);
        when(engineConfig.getType()).thenReturn("unsupported");

        // When & Then
        InvocationTargetException exception = assertThrows(InvocationTargetException.class, () -> {
            callInitMethod();
        });

        assertTrue(exception.getCause() instanceof IllegalArgumentException);
        assertEquals("Unsupported workflow engine type: unsupported", exception.getCause().getMessage());
    }

    @Test
    void init_WithNullEngineType_ThrowsException() throws Exception {
        // Given
        when(workflowConfig.getEngine()).thenReturn(engineConfig);
        when(engineConfig.getType()).thenReturn(null);

        // When & Then
        InvocationTargetException exception = assertThrows(InvocationTargetException.class, () -> {
            callInitMethod();
        });

        assertTrue(exception.getCause() instanceof NullPointerException);
    }

    @Test
    void init_WithEmptyEngineType_ThrowsException() throws Exception {
        // Given
        when(workflowConfig.getEngine()).thenReturn(engineConfig);
        when(engineConfig.getType()).thenReturn("");

        // When & Then
        InvocationTargetException exception = assertThrows(InvocationTargetException.class, () -> {
            callInitMethod();
        });

        assertTrue(exception.getCause() instanceof IllegalArgumentException);
        assertEquals("Unsupported workflow engine type: ", exception.getCause().getMessage());
    }

    @Test
    void init_WithWhitespaceEngineType_ThrowsException() throws Exception {
        // Given
        when(workflowConfig.getEngine()).thenReturn(engineConfig);
        when(engineConfig.getType()).thenReturn("   ");

        // When & Then
        InvocationTargetException exception = assertThrows(InvocationTargetException.class, () -> {
            callInitMethod();
        });

        assertTrue(exception.getCause() instanceof IllegalArgumentException);
        assertEquals("Unsupported workflow engine type: ", exception.getCause().getMessage());
    }

    @Test
    void init_WithUpperCaseEngineType_WorksCorrectly() throws Exception {
        // Given
        when(workflowConfig.getEngine()).thenReturn(engineConfig);
        when(engineConfig.getType()).thenReturn("FLOWABLE");

        // When
        callInitMethod();

        // Then
        assertNotNull(workflowEngineFactory.getWorkflowEngine());
        assertEquals(flowableWorkflowEngine, workflowEngineFactory.getWorkflowEngine());
    }

    @Test
    void init_WithMixedCaseEngineType_WorksCorrectly() throws Exception {
        // Given
        when(workflowConfig.getEngine()).thenReturn(engineConfig);
        when(engineConfig.getType()).thenReturn("Flowable");

        // When
        callInitMethod();

        // Then
        assertNotNull(workflowEngineFactory.getWorkflowEngine());
        assertEquals(flowableWorkflowEngine, workflowEngineFactory.getWorkflowEngine());
    }

    @Test
    void getWorkflowEngine_BeforeInit_ReturnsNull() {
        // When
        WorkflowEngine result = workflowEngineFactory.getWorkflowEngine();

        // Then
        assertNull(result);
    }

    @Test
    void getWorkflowEngine_AfterInit_ReturnsEngine() throws Exception {
        // Given
        when(workflowConfig.getEngine()).thenReturn(engineConfig);
        when(engineConfig.getType()).thenReturn("flowable");
        callInitMethod();

        // When
        WorkflowEngine result = workflowEngineFactory.getWorkflowEngine();

        // Then
        assertNotNull(result);
        assertEquals(flowableWorkflowEngine, result);
    }

    private void callInitMethod() throws Exception {
        Method initMethod = WorkflowEngineFactory.class.getDeclaredMethod("init");
        initMethod.setAccessible(true);
        initMethod.invoke(workflowEngineFactory);
    }
}