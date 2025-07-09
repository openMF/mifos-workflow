package org.mifos.workflow.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * Utility class for centralized exception handling across workflow services.
 * Provides consistent error handling patterns and reduces code duplication.
 */
@Component
@Slf4j
public class ExceptionHandler {

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
        switch (e) {
            case IllegalArgumentException illegalArgumentException -> {
                log.error("Invalid arguments provided for {}: {}", operation, param, e);
                return new IllegalArgumentException("Invalid arguments for " + operation + ": " + param, e);
            }
            case IllegalStateException illegalStateException -> {
                log.error("Invalid state during {}: {}", operation, param, e);
                return new IllegalStateException("Invalid state during " + operation + ": " + param, e);
            }
            case RuntimeException runtimeException -> {
                log.error("Runtime error during {}: {}", operation, param, e);
                return new RuntimeException("Runtime error during " + operation + ": " + param, e);
            }
            case null, default -> {
                log.error("Unexpected error during {}: {}", operation, param, e);
                return new RuntimeException("Unexpected error during " + operation + ": " + param, e);
            }
        }
    }
} 