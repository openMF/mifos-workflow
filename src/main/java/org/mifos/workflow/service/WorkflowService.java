package org.mifos.workflow.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mifos.workflow.config.WorkflowConfig;
import org.mifos.workflow.core.engine.WorkflowEngine;
import org.mifos.workflow.core.engine.WorkflowEngineFactory;
import org.mifos.workflow.core.model.*;
import org.mifos.workflow.service.fineract.auth.FineractAuthService;
import org.mifos.workflow.util.ExceptionHandler;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Service for managing workflow operations with integrated authentication.
 * This service provides a high-level interface for workflow management
 * and handles authentication with the Fineract system.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowService {

    private final WorkflowEngineFactory workflowEngineFactory;
    private final FineractAuthService fineractAuthService;

    @Getter
    private final WorkflowConfig workflowConfig;


    public WorkflowEngine getWorkflowEngine() {
        return workflowEngineFactory.getWorkflowEngine();
    }


    public DeploymentResult deployProcess(InputStream processDefinition, String filename) {
        log.info("Deploying process: {}", filename);

        ensureAuthentication();

        return ExceptionHandler.executeWithExceptionHandling("process deployment", filename, () -> {
            DeploymentResult result = getWorkflowEngine().deployProcess(processDefinition, filename);
            log.info("Process deployment result: {}", result.isSuccess() ? "SUCCESS" : "FAILED");
            return result;
        });
    }


    public ProcessInstance startProcess(String processDefinitionKey, Map<String, Object> variables) {
        log.info("Starting process: {} with variables: {}", processDefinitionKey, variables);

        ensureAuthentication();

        return ExceptionHandler.executeWithExceptionHandling("process start", processDefinitionKey, () -> {
            ProcessVariables processVariables = ProcessVariables.builder().variables(variables).build();

            ProcessInstance instance = getWorkflowEngine().startProcess(processDefinitionKey, processVariables);
            log.info("Started process instance: {} for definition: {}", instance.getId(), processDefinitionKey);
            return instance;
        });
    }


    public void completeTask(String taskId, Map<String, Object> variables) {
        log.info("Completing task: {} with variables: {}", taskId, variables);

        ensureAuthentication();

        ExceptionHandler.executeWithExceptionHandling("task completion", taskId, () -> {
            ProcessVariables processVariables = ProcessVariables.builder().variables(variables).build();

            getWorkflowEngine().completeTask(taskId, processVariables);
            log.info("Completed task: {}", taskId);
        });
    }


    public List<TaskInfo> getPendingTasks(String userId) {
        log.debug("Getting pending tasks for user: {}", userId);

        ensureAuthentication();

        return ExceptionHandler.executeWithExceptionHandling("getting pending tasks", userId, () -> {
            List<TaskInfo> tasks = getWorkflowEngine().getPendingTasks(userId);
            log.debug("Found {} pending tasks for user: {}", tasks.size(), userId);
            return tasks;
        });
    }


    public List<ProcessInstance> getProcessInstances() {
        log.debug("Getting all process instances");

        ensureAuthentication();

        return ExceptionHandler.executeWithExceptionHandling("getting process instances", "all", () -> {
            List<ProcessInstance> instances = getWorkflowEngine().getProcessInstances();
            log.debug("Found {} active process instances", instances.size());
            return instances;
        });
    }


    public ProcessVariables getProcessVariables(String processInstanceId) {
        log.debug("Getting variables for process instance: {}", processInstanceId);

        ensureAuthentication();

        return ExceptionHandler.executeWithExceptionHandling("getting process variables", processInstanceId, () -> {
            ProcessVariables variables = getWorkflowEngine().getProcessVariables(processInstanceId);
            log.debug("Retrieved {} variables for process instance: {}", variables.getVariables().size(), processInstanceId);
            return variables;
        });
    }


    public List<ProcessDefinition> getProcessDefinitions() {
        log.debug("Getting process definitions");

        ensureAuthentication();

        return ExceptionHandler.executeWithExceptionHandling("getting process definitions", "all", () -> {
            List<ProcessDefinition> definitions = getWorkflowEngine().getProcessDefinitions();
            log.debug("Found {} process definitions", definitions.size());
            return definitions;
        });
    }


    public boolean isEngineActive() {
        return ExceptionHandler.executeWithExceptionHandling("checking engine status", "engine", () -> getWorkflowEngine().isEngineActive());
    }


    public String getEngineType() {
        return ExceptionHandler.executeWithExceptionHandling("getting engine type", "engine", () -> getWorkflowEngine().getEngineType().name());
    }


    private void ensureAuthentication() {
        if (!workflowConfig.getAuthentication().isEnabled()) {
            log.debug("Authentication is disabled, skipping auth check");
            return;
        }

        try {
            String cachedAuthKey = fineractAuthService.getCachedAuthKey();
            if (cachedAuthKey == null || cachedAuthKey.isEmpty()) {
                log.error("Authentication required but no cached authentication key found");
                throw new IllegalStateException("Authentication required but no authentication key available. Please authenticate first.");
            } else {
                log.debug("Authentication key available for workflow operations");
            }
        } catch (IllegalStateException e) {
            log.error("Invalid state while checking authentication status", e);
            throw e;
        } catch (RuntimeException e) {
            log.error("Runtime error while checking authentication status", e);
            throw new IllegalStateException("Failed to verify authentication status", e);
        }
    }

}