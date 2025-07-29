package org.mifos.workflow.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mifos.workflow.exception.FineractApiException;
import org.mifos.workflow.exception.WorkflowException;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class WorkflowErrorHandlerTest {

    @Test
    void executeWithExceptionHandling_Success() {
        // Given
        String operation = "GET_PROCESS";
        String param = "process-123";
        String expectedResult = "success";

        // When
        String result = WorkflowErrorHandler.executeWithExceptionHandling(operation, param, () -> expectedResult);

        // Then
        assertEquals(expectedResult, result);
    }

    @Test
    void executeWithExceptionHandling_TaskNotFound() {
        // Given
        String operation = "GET_TASK";
        String param = "task-456";
        RuntimeException taskNotFoundException = new RuntimeException("Cannot find task with id task-456");

        // When & Then
        WorkflowException exception = assertThrows(WorkflowException.class, () -> {
            WorkflowErrorHandler.executeWithExceptionHandling(operation, param, () -> {
                throw taskNotFoundException;
            });
        });

        assertEquals("Task not found: task-456", exception.getMessage());
        assertEquals(operation, exception.getOperation());
        assertEquals(WorkflowException.ERROR_TASK_NOT_FOUND, exception.getErrorCode());
        assertEquals(param, exception.getTaskId());
    }

    @Test
    void executeWithExceptionHandling_ProcessNotFound() {
        // Given
        String operation = "GET_PROCESS";
        String param = "process-789";
        RuntimeException processNotFoundException = new RuntimeException("Cannot find process instance with id process-789");

        // When & Then
        WorkflowException exception = assertThrows(WorkflowException.class, () -> {
            WorkflowErrorHandler.executeWithExceptionHandling(operation, param, () -> {
                throw processNotFoundException;
            });
        });

        assertEquals("Process instance not found: process-789", exception.getMessage());
        assertEquals(operation, exception.getOperation());
        assertEquals(WorkflowException.ERROR_PROCESS_NOT_FOUND, exception.getErrorCode());
        assertEquals(param, exception.getProcessId());
    }

    @Test
    void executeWithExceptionHandling_ProcessDefinitionNotFound() {
        // Given
        String operation = "GET_PROCESS_DEFINITION";
        String param = "def-123";
        RuntimeException processDefNotFoundException = new RuntimeException("Cannot find process definition with id def-123");

        // When & Then
        WorkflowException exception = assertThrows(WorkflowException.class, () -> {
            WorkflowErrorHandler.executeWithExceptionHandling(operation, param, () -> {
                throw processDefNotFoundException;
            });
        });

        assertEquals("Process definition not found: def-123", exception.getMessage());
        assertEquals(operation, exception.getOperation());
        assertEquals(WorkflowException.ERROR_PROCESS_DEFINITION_NOT_FOUND, exception.getErrorCode());
        assertEquals(param, exception.getProcessId());
    }

    @Test
    void executeWithExceptionHandling_DeploymentNotFound() {
        // Given
        String operation = "GET_DEPLOYMENT";
        String param = "deploy-456";
        RuntimeException deploymentNotFoundException = new RuntimeException("Cannot find deployment with id deploy-456");

        // When & Then
        WorkflowException exception = assertThrows(WorkflowException.class, () -> {
            WorkflowErrorHandler.executeWithExceptionHandling(operation, param, () -> {
                throw deploymentNotFoundException;
            });
        });

        assertEquals("Deployment not found: deploy-456", exception.getMessage());
        assertEquals(operation, exception.getOperation());
        assertEquals(WorkflowException.ERROR_DEPLOYMENT_NOT_FOUND, exception.getErrorCode());
        assertEquals(param, exception.getProcessId());
    }

    @Test
    void executeWithExceptionHandling_ProcessAlreadyEnded() {
        // Given
        String operation = "TERMINATE_PROCESS";
        String param = "process-123";
        RuntimeException processEndedException = new RuntimeException("Process instance is already ended process-123");

        // When & Then
        WorkflowException exception = assertThrows(WorkflowException.class, () -> {
            WorkflowErrorHandler.executeWithExceptionHandling(operation, param, () -> {
                throw processEndedException;
            });
        });

        assertEquals("Process instance is already ended: process-123", exception.getMessage());
        assertEquals(operation, exception.getOperation());
        assertEquals(WorkflowException.ERROR_INVALID_PROCESS_STATE, exception.getErrorCode());
        assertEquals(param, exception.getProcessId());
    }

    @Test
    void executeWithExceptionHandling_TaskAlreadyCompleted() {
        // Given
        String operation = "COMPLETE_TASK";
        String param = "task-456";
        RuntimeException taskCompletedException = new RuntimeException("Task is already completed task-456");

        // When & Then
        WorkflowException exception = assertThrows(WorkflowException.class, () -> {
            WorkflowErrorHandler.executeWithExceptionHandling(operation, param, () -> {
                throw taskCompletedException;
            });
        });

        assertEquals("Task is already completed: task-456", exception.getMessage());
        assertEquals(operation, exception.getOperation());
        assertEquals(WorkflowException.ERROR_INVALID_TASK_STATE, exception.getErrorCode());
        assertEquals(param, exception.getTaskId());
    }

    @Test
    void executeWithExceptionHandling_HistoricProcessNotFound() {
        // Given
        String operation = "GET_HISTORIC_PROCESS";
        String param = "process-123";
        RuntimeException historicProcessNotFoundException = new RuntimeException("Historic process instance not found process-123");

        // When & Then
        WorkflowException exception = assertThrows(WorkflowException.class, () -> {
            WorkflowErrorHandler.executeWithExceptionHandling(operation, param, () -> {
                throw historicProcessNotFoundException;
            });
        });

        assertEquals("Historic process instance not found: process-123", exception.getMessage());
        assertEquals(operation, exception.getOperation());
        assertEquals(WorkflowException.ERROR_PROCESS_NOT_FOUND, exception.getErrorCode());
        assertEquals(param, exception.getProcessId());
    }

    @Test
    void executeWithExceptionHandling_HistoricVariableNotFound() {
        // Given
        String operation = "GET_HISTORIC_VARIABLE";
        String param = "var-123";
        RuntimeException historicVariableNotFoundException = new RuntimeException("Historic variable instance not found var-123");

        // When & Then
        WorkflowException exception = assertThrows(WorkflowException.class, () -> {
            WorkflowErrorHandler.executeWithExceptionHandling(operation, param, () -> {
                throw historicVariableNotFoundException;
            });
        });

        assertEquals("Historic variable instance not found: var-123", exception.getMessage());
        assertEquals(operation, exception.getOperation());
        assertEquals(WorkflowException.ERROR_PROCESS_NOT_FOUND, exception.getErrorCode());
        assertEquals(param, exception.getProcessId());
    }

    @Test
    void executeWithExceptionHandling_FineractApiException() {
        // Given
        String operation = "GET_CLIENT";
        String param = "client-123";
        FineractApiException fineractException = new FineractApiException("Client not found", new RuntimeException(), operation, param);

        // When & Then
        FineractApiException exception = assertThrows(FineractApiException.class, () -> {
            WorkflowErrorHandler.executeWithExceptionHandling(operation, param, () -> {
                throw fineractException;
            });
        });

        assertEquals("Client not found", exception.getMessage());
        assertEquals(operation, exception.getOperation());
        assertEquals(param, exception.getResourceId());
    }

    @Test
    void executeWithExceptionHandling_WorkflowException() {
        // Given
        String operation = "GET_PROCESS";
        String param = "process-123";
        WorkflowException workflowException = new WorkflowException("Process not found", operation, "PROCESS_NOT_FOUND", param, null);

        // When & Then
        WorkflowException exception = assertThrows(WorkflowException.class, () -> {
            WorkflowErrorHandler.executeWithExceptionHandling(operation, param, () -> {
                throw workflowException;
            });
        });

        assertEquals("Process not found", exception.getMessage());
        assertEquals(operation, exception.getOperation());
        assertEquals("PROCESS_NOT_FOUND", exception.getErrorCode());
        assertEquals(param, exception.getProcessId());
    }

    @Test
    void executeWithExceptionHandling_IllegalArgumentException() {
        // Given
        String operation = "GET_PROCESS";
        String param = "invalid-param";
        IllegalArgumentException illegalArgumentException = new IllegalArgumentException("Invalid parameter");

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            WorkflowErrorHandler.executeWithExceptionHandling(operation, param, () -> {
                throw illegalArgumentException;
            });
        });

        assertEquals("Invalid arguments for GET_PROCESS: invalid-param", exception.getMessage());
    }

    @Test
    void executeWithExceptionHandling_IllegalStateException() {
        // Given
        String operation = "START_PROCESS";
        String param = "process-123";
        IllegalStateException illegalStateException = new IllegalStateException("Invalid state");

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            WorkflowErrorHandler.executeWithExceptionHandling(operation, param, () -> {
                throw illegalStateException;
            });
        });

        assertEquals("Invalid state during START_PROCESS: process-123", exception.getMessage());
    }

    @Test
    void executeWithExceptionHandling_RuntimeException() {
        // Given
        String operation = "GET_PROCESS";
        String param = "process-123";
        RuntimeException runtimeException = new RuntimeException("Runtime error");

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            WorkflowErrorHandler.executeWithExceptionHandling(operation, param, () -> {
                throw runtimeException;
            });
        });

        assertEquals("Runtime error during GET_PROCESS: process-123", exception.getMessage());
    }

    @Test
    void executeWithExceptionHandling_GenericException() {
        // Given
        String operation = "GET_PROCESS";
        String param = "process-123";
        Exception genericException = new Exception("Generic error");

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            WorkflowErrorHandler.executeWithExceptionHandling(operation, param, () -> {
                try {
                    throw genericException;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        });

        assertEquals("Runtime error during GET_PROCESS: process-123", exception.getMessage());
    }

    @Test
    void executeWithExceptionHandling_Void_Success() {
        // Given
        String operation = "DELETE_PROCESS";
        String param = "process-123";

        // When & Then - should not throw any exception
        assertDoesNotThrow(() -> {
            WorkflowErrorHandler.executeWithExceptionHandling(operation, param, () -> {
                // Simulate successful operation
            });
        });
    }

    @Test
    void executeWithExceptionHandling_Void_WithException() {
        // Given
        String operation = "DELETE_PROCESS";
        String param = "process-123";
        RuntimeException runtimeException = new RuntimeException("Delete failed");

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            WorkflowErrorHandler.executeWithExceptionHandling(operation, param, () -> {
                throw runtimeException;
            });
        });

        assertEquals("Runtime error during DELETE_PROCESS: process-123", exception.getMessage());
    }

    @Test
    void executeWithExceptionHandling_NullMessage() {
        // Given
        String operation = "GET_PROCESS";
        String param = "process-123";
        RuntimeException exceptionWithNullMessage = new RuntimeException();

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            WorkflowErrorHandler.executeWithExceptionHandling(operation, param, () -> {
                throw exceptionWithNullMessage;
            });
        });

        assertEquals("Runtime error during GET_PROCESS: process-123", exception.getMessage());
    }
}