package org.mifos.workflow.util;

import lombok.extern.slf4j.Slf4j;
import org.mifos.workflow.exception.WorkflowException;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Utility class for centralized exception handling across workflow services.
 * Provides consistent error handling patterns and reduces code duplication.
 */
@Component
@Slf4j
public class WorkflowErrorHandler {

    private static final Map<String, ErrorPattern> ERROR_PATTERNS = new HashMap<>();

    static {
        registerTaskNotFoundPattern();
        registerProcessNotFoundPattern();
        registerProcessDefinitionNotFoundPattern();
        registerDeploymentNotFoundPattern();
        registerProcessStatePatterns();
        registerHistoricPatterns();
    }

    private static void registerTaskNotFoundPattern() {
        registerErrorPattern(
            "Cannot find task with id",
            (operation, param) -> new WorkflowException("Task not found: " + param, operation, WorkflowException.ERROR_TASK_NOT_FOUND, null, param),
            message -> message.contains("Cannot find task with id")
        );
    }

    private static void registerProcessNotFoundPattern() {
        registerErrorPattern(
            "Cannot find process instance with id",
            (operation, param) -> new WorkflowException("Process instance not found: " + param, operation, WorkflowException.ERROR_PROCESS_NOT_FOUND, param, null),
            message -> message.contains("Cannot find process instance with id")
        );

        registerErrorPattern(
            "execution",
            (operation, param) -> new WorkflowException("Process execution not found: " + param, operation, WorkflowException.ERROR_PROCESS_NOT_FOUND, param, null),
            message -> message.contains("execution") && message.contains("doesn't exist")
        );
    }

    private static void registerProcessDefinitionNotFoundPattern() {
        registerErrorPattern(
            "Cannot find process definition with id",
            (operation, param) -> new WorkflowException("Process definition not found: " + param, operation, WorkflowException.ERROR_PROCESS_DEFINITION_NOT_FOUND, param, null),
            message -> message.contains("Cannot find process definition with id")
        );
    }

    private static void registerDeploymentNotFoundPattern() {
        registerErrorPattern(
            "Cannot find deployment with id",
            (operation, param) -> new WorkflowException("Deployment not found: " + param, operation, WorkflowException.ERROR_DEPLOYMENT_NOT_FOUND, param, null),
            message -> message.contains("Cannot find deployment with id")
        );
    }

    private static void registerProcessStatePatterns() {
        registerErrorPattern(
            "Process instance is already ended",
            (operation, param) -> new WorkflowException("Process instance is already ended: " + param, operation, WorkflowException.ERROR_INVALID_PROCESS_STATE, param, null),
            message -> message.contains("Process instance is already ended")
        );

        registerErrorPattern(
            "Task is already completed",
            (operation, param) -> new WorkflowException("Task is already completed: " + param, operation, WorkflowException.ERROR_INVALID_TASK_STATE, null, param),
            message -> message.contains("Task is already completed")
        );
    }

    private static void registerHistoricPatterns() {
        registerErrorPattern(
            "Historic process instance not found",
            (operation, param) -> new WorkflowException("Historic process instance not found: " + param, operation, WorkflowException.ERROR_PROCESS_NOT_FOUND, param, null),
            message -> message.contains("Historic process instance not found") || message.contains("doesn't exist")
        );

        registerErrorPattern(
            "Historic variable instance not found",
            (operation, param) -> new WorkflowException("Historic variable instance not found: " + param, operation, WorkflowException.ERROR_PROCESS_NOT_FOUND, param, null),
            message -> message.contains("Historic variable instance not found") || message.contains("variable") && message.contains("not found")
        );
    }

    public static <T> T executeWithExceptionHandling(String operation, String param, Supplier<T> operationSupplier) {
        try {
            return operationSupplier.get();
        } catch (Exception e) {
            throw handleException(e, operation, param);
        }
    }

    public static void executeWithExceptionHandling(String operation, String param, Runnable operationRunnable) {
        try {
            operationRunnable.run();
        } catch (Exception e) {
            throw handleException(e, operation, param);
        }
    }

    private static RuntimeException handleException(Exception e, String operation, String param) {
        if (e instanceof org.mifos.workflow.exception.FineractApiException) {
            log.error("Fineract API error during {}: {}", operation, e.getMessage(), e);
            return (RuntimeException) e;
        }
        if (e instanceof org.mifos.workflow.exception.WorkflowException) {
            log.error("Workflow error during {}: {}", operation, e.getMessage(), e);
            return (RuntimeException) e;
        }

        String message = e.getMessage();
        if (message != null) {
            WorkflowException workflowException = matchErrorPatterns(message, operation, param);
            if (workflowException != null) {
                return workflowException;
            }
        }

        return handleStandardException(e, operation, param);
    }

    private static WorkflowException matchErrorPatterns(String message, String operation, String param) {
        for (Map.Entry<String, ErrorPattern> entry : ERROR_PATTERNS.entrySet()) {
            String pattern = entry.getKey();
            ErrorPattern errorPattern = entry.getValue();

            if (errorPattern.matches(message)) {
                log.error("{} error during {}: {}", pattern, operation, param);
                return errorPattern.createException(operation, param);
            }
        }
        return null;
    }

    private static RuntimeException handleStandardException(Exception e, String operation, String param) {
        return switch (e) {
            case IllegalArgumentException ignored -> {
                log.error("Invalid arguments provided for {}: {}", operation, param, e);
                yield new IllegalArgumentException("Invalid arguments for " + operation + ": " + param, e);
            }
            case IllegalStateException ignored -> {
                log.error("Invalid state during {}: {}", operation, param, e);
                yield new IllegalStateException("Invalid state during " + operation + ": " + param, e);
            }
            case RuntimeException ignored -> {
                log.error("Runtime error during {}: {}", operation, param, e);
                yield new RuntimeException("Runtime error during " + operation + ": " + param, e);
            }
            default -> {
                log.error("Unexpected error during {}: {}", operation, param, e);
                yield new RuntimeException("Unexpected error during " + operation + ": " + param, e);
            }
        };
    }


    private static void registerErrorPattern(String pattern, BiFunction<String, String, WorkflowException> exceptionCreator, Function<String, Boolean> matcher) {
        ERROR_PATTERNS.put(pattern, new ErrorPattern(exceptionCreator, matcher));
    }

    private record ErrorPattern(BiFunction<String, String, WorkflowException> exceptionCreator,
                                Function<String, Boolean> matcher) {

        public boolean matches(String message) {
            return matcher.apply(message);
        }

        public WorkflowException createException(String operation, String param) {
            return exceptionCreator.apply(operation, param);
        }
    }
}