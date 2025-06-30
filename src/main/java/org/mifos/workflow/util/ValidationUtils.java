package org.mifos.workflow.util;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * Utility class for parameter validation.
 * Provides a simple way to validate @NotNull parameters without AOP complexity.
 */
public class ValidationUtils {
    
    /**
     * Validates that all @NotNull parameters are not null.
     * This method should be called at the beginning of each service method.
     * 
     * @param method The method being called
     * @param args The arguments passed to the method
     */
    public static void validateNotNullParameters(Method method, Object[] args) {
        Parameter[] parameters = method.getParameters();
        
        for (int i = 0; i < args.length && i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            Object arg = args[i];
            
            if (parameter.isAnnotationPresent(jakarta.validation.constraints.NotNull.class) && arg == null) {
                String paramName = parameter.getName();
                throw new IllegalArgumentException(paramName + " must not be null");
            }
        }
    }
} 