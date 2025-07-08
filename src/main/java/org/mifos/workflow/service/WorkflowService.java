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
        } catch (Exception e) {
            log.error("Failed to deploy process: {}", filename, e);
            throw new RuntimeException("Process deployment failed: " + filename, e);
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
        } catch (Exception e) {
            log.error("Failed to start process: {}", processDefinitionKey, e);
            throw new RuntimeException("Process start failed: " + processDefinitionKey, e);
        }
    }


    public void completeTask(String taskId, Map<String, Object> variables) {
        log.info("Completing task: {} with variables: {}", taskId, variables);

        ensureAuthentication();

        try {
            ProcessVariables processVariables = ProcessVariables.builder().variables(variables).build();

            getWorkflowEngine().completeTask(taskId, processVariables);
            log.info("Completed task: {}", taskId);
        } catch (Exception e) {
            log.error("Failed to complete task: {}", taskId, e);
            throw new RuntimeException("Task completion failed: " + taskId, e);
        }
    }


    public List<TaskInfo> getPendingTasks(String userId) {
        log.debug("Getting pending tasks for user: {}", userId);

        ensureAuthentication();

        try {
            List<TaskInfo> tasks = getWorkflowEngine().getPendingTasks(userId);
            log.debug("Found {} pending tasks for user: {}", tasks.size(), userId);
            return tasks;
        } catch (Exception e) {
            log.error("Failed to get pending tasks for user: {}", userId, e);
            throw new RuntimeException("Failed to get pending tasks for user: " + userId, e);
        }
    }


    public List<ProcessInstance> getProcessInstances() {
        log.debug("Getting all process instances");

        ensureAuthentication();

        try {
            List<ProcessInstance> instances = getWorkflowEngine().getProcessInstances();
            log.debug("Found {} active process instances", instances.size());
            return instances;
        } catch (Exception e) {
            log.error("Failed to get process instances", e);
            throw new RuntimeException("Failed to get process instances", e);
        }
    }


    public ProcessVariables getProcessVariables(String processInstanceId) {
        log.debug("Getting variables for process instance: {}", processInstanceId);

        ensureAuthentication();

        try {
            ProcessVariables variables = getWorkflowEngine().getProcessVariables(processInstanceId);
            log.debug("Retrieved {} variables for process instance: {}", variables.getVariables().size(), processInstanceId);
            return variables;
        } catch (Exception e) {
            log.error("Failed to get process variables for instance: {}", processInstanceId, e);
            throw new RuntimeException("Failed to get process variables for instance: " + processInstanceId, e);
        }
    }


    public List<ProcessDefinition> getProcessDefinitions() {
        log.debug("Getting process definitions");

        ensureAuthentication();

        try {
            List<ProcessDefinition> definitions = getWorkflowEngine().getProcessDefinitions();
            log.debug("Found {} process definitions", definitions.size());
            return definitions;
        } catch (Exception e) {
            log.error("Failed to get process definitions", e);
            throw new RuntimeException("Failed to get process definitions", e);
        }
    }


    public boolean isEngineActive() {
        try {
            return getWorkflowEngine().isEngineActive();
        } catch (Exception e) {
            log.error("Failed to check engine status", e);
            return false;
        }
    }


    public String getEngineType() {
        try {
            return getWorkflowEngine().getEngineType().name();
        } catch (Exception e) {
            log.error("Failed to get engine type", e);
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
        } catch (Exception e) {
            log.error("Failed to check authentication status", e);
        }
    }

}