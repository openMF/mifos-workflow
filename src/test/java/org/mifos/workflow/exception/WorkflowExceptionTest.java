package org.mifos.workflow.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WorkflowExceptionTest {

    @Test
    void constructor_WithMessageOperationErrorCodeProcessTask_SetsFields() {
        // Given
        String message = "Workflow processing failed";
        String operation = "startProcess";
        String errorCode = WorkflowException.ERROR_WORKFLOW_ENGINE_ERROR;
        String processId = "proc-1";
        String taskId = "task-1";

        // When
        WorkflowException exception = new WorkflowException(message, operation, errorCode, processId, taskId);

        // Then
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertEquals(operation, exception.getOperation());
        assertEquals(errorCode, exception.getErrorCode());
        assertEquals(processId, exception.getProcessId());
        assertEquals(taskId, exception.getTaskId());
    }

    @Test
    void constructor_WithCauseOperationErrorCode_SetsFields() {
        // Given
        String message = "Workflow processing failed";
        Throwable cause = new RuntimeException("Root cause");
        String operation = "completeTask";
        String errorCode = WorkflowException.ERROR_INVALID_TASK_STATE;

        // When
        WorkflowException exception = new WorkflowException(message, cause, operation, errorCode);

        // Then
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertEquals(operation, exception.getOperation());
        assertEquals(errorCode, exception.getErrorCode());
        assertNull(exception.getProcessId());
        assertNull(exception.getTaskId());
    }

    @Test
    void constructor_WithFullParams_SetsAllFields() {
        // Given
        String message = "Workflow error";
        Throwable cause = new IllegalStateException("Illegal state");
        String operation = "approveLoan";
        String errorCode = WorkflowException.ERROR_LOAN_APPROVAL_FAILED;
        String processId = "proc-2";
        String taskId = "task-9";

        // When
        WorkflowException exception = new WorkflowException(message, cause, operation, errorCode, processId, taskId);

        // Then
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertEquals(operation, exception.getOperation());
        assertEquals(errorCode, exception.getErrorCode());
        assertEquals(processId, exception.getProcessId());
        assertEquals(taskId, exception.getTaskId());
    }

    @Test
    void toString_ReturnsValidString() {
        // Given
        WorkflowException exception = new WorkflowException(
            "Error occurred",
            "startProcess",
            WorkflowException.ERROR_PROCESS_EXECUTION_ERROR,
            "proc-123",
            "task-456"
        );

        // When
        String result = exception.toString();

        // Then
        assertNotNull(result);
        assertTrue(result.contains("WorkflowException"));
        assertTrue(result.contains("proc-123"));
        assertTrue(result.contains("task-456"));
    }
}
