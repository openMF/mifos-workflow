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
        } catch (IllegalArgumentException e) {
            log.error("Invalid arguments provided for {}: {}", operation, param, e);
            throw new IllegalArgumentException("Invalid arguments for " + operation + ": " + param, e);
        } catch (IllegalStateException e) {
            log.error("Invalid state during {}: {}", operation, param, e);
            throw new IllegalStateException("Invalid state during " + operation + ": " + param, e);
        } catch (RuntimeException e) {
            log.error("Runtime error during {}: {}", operation, param, e);
            throw new RuntimeException("Runtime error during " + operation + ": " + param, e);
        }
    }


    public static void executeWithExceptionHandling(String operation, String param, Runnable operationRunnable) {
        try {
            operationRunnable.run();
        } catch (IllegalArgumentException e) {
            log.error("Invalid arguments provided for {}: {}", operation, param, e);
            throw new IllegalArgumentException("Invalid arguments for " + operation + ": " + param, e);
        } catch (IllegalStateException e) {
            log.error("Invalid state during {}: {}", operation, param, e);
            throw new IllegalStateException("Invalid state during " + operation + ": " + param, e);
        } catch (RuntimeException e) {
            log.error("Runtime error during {}: {}", operation, param, e);
            throw new RuntimeException("Runtime error during " + operation + ": " + param, e);
        }
    }
} 