package org.mifos.workflow.config;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

/**
 * AOP aspect for automatic parameter validation.
 * Validates @NotNull parameters before method execution.
 */
@Aspect
@Component
@Slf4j
public class ValidationAspectConfig {

    public ValidationAspectConfig() {
        log.info("ValidationAspectConfig constructor called - AOP aspect is being created!");
    }

    @Before("execution(* org.mifos.workflow.service.fineract.client.FineractClientService.*(..))")
    public void validateNotNullParameters(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        log.info("AOP aspect triggered for method: {} with {} args", methodName, args.length);
        for (int i = 0; i < args.length; i++) {
            if (args[i] == null) {
                log.info("Null argument detected at position {} in method {}", i, methodName);
                throw new IllegalArgumentException("Parameter at position " + i + " must not be null");
            }
        }
    }
} 