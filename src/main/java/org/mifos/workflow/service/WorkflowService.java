package org.mifos.workflow.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mifos.workflow.config.WorkflowConfig;
import org.mifos.workflow.core.engine.WorkflowEngine;
import org.mifos.workflow.core.engine.WorkflowEngineFactory;
import org.mifos.workflow.core.model.*;
import org.mifos.workflow.service.fineract.auth.FineractAuthService;
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

        try {
            DeploymentResult result = getWorkflowEngine().deployProcess(processDefinition, filename);
            log.info("Process deployment result: {}", result.isSuccess() ? "SUCCESS" : "FAILED");
            return result;
        } catch (IllegalArgumentException e) {
            log.error("Invalid arguments provided for process deployment: {}", filename, e);
            throw new IllegalArgumentException("Invalid arguments for process deployment: " + filename, e);
        } catch (IllegalStateException e) {
            log.error("Invalid state during process deployment: {}", filename, e);
            throw new IllegalStateException("Invalid state during process deployment: " + filename, e);
        } catch (RuntimeException e) {
            log.error("Runtime error during process deployment: {}", filename, e);
            throw new RuntimeException("Runtime error during process deployment: " + filename, e);
        }
    }


    public ProcessInstance startProcess(String processDefinitionKey, Map<String, Object> variables) {
        log.info("Starting process: {} with variables: {}", processDefinitionKey, variables);

        ensureAuthentication();

        try {
            ProcessVariables processVariables = ProcessVariables.builder().variables(variables).build();

            ProcessInstance instance = getWorkflowEngine().startProcess(processDefinitionKey, processVariables);
            log.info("Started process instance: {} for definition: {}", instance.getId(), processDefinitionKey);
            return instance;
        } catch (IllegalArgumentException e) {
            log.error("Invalid arguments provided for process start: {}", processDefinitionKey, e);
            throw new IllegalArgumentException("Invalid arguments for process start: " + processDefinitionKey, e);
        } catch (IllegalStateException e) {
            log.error("Invalid state during process start: {}", processDefinitionKey, e);
            throw new IllegalStateException("Invalid state during process start: " + processDefinitionKey, e);
        } catch (RuntimeException e) {
            log.error("Runtime error during process start: {}", processDefinitionKey, e);
            throw new RuntimeException("Runtime error during process start: " + processDefinitionKey, e);
        }
    }


    public void completeTask(String taskId, Map<String, Object> variables) {
        log.info("Completing task: {} with variables: {}", taskId, variables);

        ensureAuthentication();

        try {
            ProcessVariables processVariables = ProcessVariables.builder().variables(variables).build();

            getWorkflowEngine().completeTask(taskId, processVariables);
            log.info("Completed task: {}", taskId);
        } catch (IllegalArgumentException e) {
            log.error("Invalid arguments provided for task completion: {}", taskId, e);
            throw new IllegalArgumentException("Invalid arguments for task completion: " + taskId, e);
        } catch (IllegalStateException e) {
            log.error("Invalid state during task completion: {}", taskId, e);
            throw new IllegalStateException("Invalid state during task completion: " + taskId, e);
        } catch (RuntimeException e) {
            log.error("Runtime error during task completion: {}", taskId, e);
            throw new RuntimeException("Runtime error during task completion: " + taskId, e);
        }
    }


    public List<TaskInfo> getPendingTasks(String userId) {
        log.debug("Getting pending tasks for user: {}", userId);

        ensureAuthentication();

        try {
            List<TaskInfo> tasks = getWorkflowEngine().getPendingTasks(userId);
            log.debug("Found {} pending tasks for user: {}", tasks.size(), userId);
            return tasks;
        } catch (IllegalArgumentException e) {
            log.error("Invalid arguments provided for getting pending tasks: {}", userId, e);
            throw new IllegalArgumentException("Invalid arguments for getting pending tasks: " + userId, e);
        } catch (IllegalStateException e) {
            log.error("Invalid state while getting pending tasks: {}", userId, e);
            throw new IllegalStateException("Invalid state while getting pending tasks: " + userId, e);
        } catch (RuntimeException e) {
            log.error("Runtime error while getting pending tasks: {}", userId, e);
            throw new RuntimeException("Runtime error while getting pending tasks: " + userId, e);
        }
    }


    public List<ProcessInstance> getProcessInstances() {
        log.debug("Getting all process instances");

        ensureAuthentication();

        try {
            List<ProcessInstance> instances = getWorkflowEngine().getProcessInstances();
            log.debug("Found {} active process instances", instances.size());
            return instances;
        } catch (IllegalArgumentException e) {
            log.error("Invalid arguments provided for getting process instances", e);
            throw new IllegalArgumentException("Invalid arguments for getting process instances", e);
        } catch (IllegalStateException e) {
            log.error("Invalid state while getting process instances", e);
            throw new IllegalStateException("Invalid state while getting process instances", e);
        } catch (RuntimeException e) {
            log.error("Runtime error while getting process instances", e);
            throw new RuntimeException("Runtime error while getting process instances", e);
        }
    }


    public ProcessVariables getProcessVariables(String processInstanceId) {
        log.debug("Getting variables for process instance: {}", processInstanceId);

        ensureAuthentication();

        try {
            ProcessVariables variables = getWorkflowEngine().getProcessVariables(processInstanceId);
            log.debug("Retrieved {} variables for process instance: {}", variables.getVariables().size(), processInstanceId);
            return variables;
        } catch (IllegalArgumentException e) {
            log.error("Invalid arguments provided for getting process variables: {}", processInstanceId, e);
            throw new IllegalArgumentException("Invalid arguments for getting process variables: " + processInstanceId, e);
        } catch (IllegalStateException e) {
            log.error("Invalid state while getting process variables: {}", processInstanceId, e);
            throw new IllegalStateException("Invalid state while getting process variables: " + processInstanceId, e);
        } catch (RuntimeException e) {
            log.error("Runtime error while getting process variables: {}", processInstanceId, e);
            throw new RuntimeException("Runtime error while getting process variables: " + processInstanceId, e);
        }
    }


    public List<ProcessDefinition> getProcessDefinitions() {
        log.debug("Getting process definitions");

        ensureAuthentication();

        try {
            List<ProcessDefinition> definitions = getWorkflowEngine().getProcessDefinitions();
            log.debug("Found {} process definitions", definitions.size());
            return definitions;
        } catch (IllegalArgumentException e) {
            log.error("Invalid arguments provided for getting process definitions", e);
            throw new IllegalArgumentException("Invalid arguments for getting process definitions", e);
        } catch (IllegalStateException e) {
            log.error("Invalid state while getting process definitions", e);
            throw new IllegalStateException("Invalid state while getting process definitions", e);
        } catch (RuntimeException e) {
            log.error("Runtime error while getting process definitions", e);
            throw new RuntimeException("Runtime error while getting process definitions", e);
        }
    }


    public boolean isEngineActive() {
        try {
            return getWorkflowEngine().isEngineActive();
        } catch (IllegalStateException e) {
            log.error("Invalid state while checking engine status", e);
            return false;
        } catch (RuntimeException e) {
            log.error("Runtime error while checking engine status", e);
            return false;
        }
    }


    public String getEngineType() {
        try {
            return getWorkflowEngine().getEngineType().name();
        } catch (IllegalStateException e) {
            log.error("Invalid state while getting engine type", e);
            return "UNKNOWN";
        } catch (RuntimeException e) {
            log.error("Runtime error while getting engine type", e);
            return "UNKNOWN";
        }
    }


    private void ensureAuthentication() {
        if (!workflowConfig.getAuthentication().isEnabled()) {
            log.debug("Authentication is disabled, skipping auth check");
            return;
        }

        try {
            String cachedAuthKey = fineractAuthService.getCachedAuthKey();
            if (cachedAuthKey == null || cachedAuthKey.isEmpty()) {
                log.info("No cached authentication key found, authentication required");
                log.warn("Authentication key not available - workflow operations may fail");
            } else {
                log.debug("Authentication key available for workflow operations");
            }
        } catch (IllegalStateException e) {
            log.error("Invalid state while checking authentication status", e);
        } catch (RuntimeException e) {
            log.error("Runtime error while checking authentication status", e);
        }
    }

}