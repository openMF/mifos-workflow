package org.mifos.workflow.exception;

import lombok.Getter;

/**
 * Custom exception for workflow-related errors.
 * Provides detailed error information including process context and operation details.
 *
 * <p>This exception is thrown when workflow operations fail, providing comprehensive
 * error details for debugging and error handling in the workflow engine.</p>
 */
@Getter
public class WorkflowException extends RuntimeException {

    private final String processId;
    private final String taskId;
    private final String operation;
    private final String errorCode;

    public static final String ERROR_TASK_NOT_FOUND = "TASK_NOT_FOUND";
    public static final String ERROR_PROCESS_NOT_FOUND = "PROCESS_NOT_FOUND";
    public static final String ERROR_PROCESS_DEFINITION_NOT_FOUND = "PROCESS_DEFINITION_NOT_FOUND";
    public static final String ERROR_DEPLOYMENT_NOT_FOUND = "DEPLOYMENT_NOT_FOUND";
    public static final String ERROR_INVALID_PROCESS_STATE = "INVALID_PROCESS_STATE";
    public static final String ERROR_INVALID_TASK_STATE = "INVALID_TASK_STATE";
    public static final String ERROR_WORKFLOW_ENGINE_ERROR = "WORKFLOW_ENGINE_ERROR";
    public static final String ERROR_PROCESS_EXECUTION_ERROR = "PROCESS_EXECUTION_ERROR";


    public WorkflowException(String message, String operation, String errorCode, String processId, String taskId) {
        super(message);
        this.operation = operation;
        this.errorCode = errorCode;
        this.processId = processId;
        this.taskId = taskId;
    }


    public WorkflowException(String message, Throwable cause, String operation, String errorCode) {
        super(message, cause);
        this.operation = operation;
        this.errorCode = errorCode;
        this.processId = null;
        this.taskId = null;
    }


    public WorkflowException(String message, Throwable cause, String operation, String errorCode, String processId, String taskId) {
        super(message, cause);
        this.operation = operation;
        this.errorCode = errorCode;
        this.processId = processId;
        this.taskId = taskId;
    }


    @Override
    public String toString() {
        return String.format("WorkflowException{errorCode='%s', operation='%s', processId='%s', taskId='%s', message='%s'}", errorCode, operation, processId, taskId, getMessage());
    }
} 