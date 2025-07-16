package org.mifos.workflow.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mifos.workflow.config.WorkflowConfig;
import org.mifos.workflow.core.engine.WorkflowEngine;
import org.mifos.workflow.core.engine.WorkflowEngineFactory;
import org.mifos.workflow.core.model.ActiveProcess;
import org.mifos.workflow.core.model.DeploymentInfo;
import org.mifos.workflow.core.model.DeploymentInfoEnhanced;
import org.mifos.workflow.core.model.DeploymentResource;
import org.mifos.workflow.core.model.DeploymentResult;
import org.mifos.workflow.core.model.ProcessCompletionStatus;
import org.mifos.workflow.core.model.ProcessDefinition;
import org.mifos.workflow.core.model.ProcessDefinitionInfo;
import org.mifos.workflow.core.model.ProcessHistoryInfo;
import org.mifos.workflow.core.model.ProcessInstance;
import org.mifos.workflow.core.model.ProcessStatus;
import org.mifos.workflow.core.model.ProcessVariables;
import org.mifos.workflow.core.model.TaskInfo;
import org.mifos.workflow.service.fineract.auth.FineractAuthService;
import org.mifos.workflow.util.WorkflowErrorHandler;
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

        return WorkflowErrorHandler.executeWithExceptionHandling("process deployment", filename, () -> {
            DeploymentResult result = getWorkflowEngine().deployProcess(processDefinition, filename);
            log.info("Process deployment result: {}", result.isSuccess() ? "SUCCESS" : "FAILED");
            return result;
        });
    }


    public ProcessInstance startProcess(String processDefinitionKey, Map<String, Object> variables) {
        log.info("Starting process: {} with variables: {}", processDefinitionKey, variables);

        ensureAuthentication();

        return WorkflowErrorHandler.executeWithExceptionHandling("process start", processDefinitionKey, () -> {
            ProcessVariables processVariables = ProcessVariables.builder().variables(variables).build();

            ProcessInstance instance = getWorkflowEngine().startProcess(processDefinitionKey, processVariables);
            log.info("Started process instance: {} for definition: {}", instance.getId(), processDefinitionKey);
            return instance;
        });
    }


    public void completeTask(String taskId, Map<String, Object> variables) {
        log.info("Completing task: {} with variables: {}", taskId, variables);

        ensureAuthentication();

        WorkflowErrorHandler.executeWithExceptionHandling("task completion", taskId, () -> {
            ProcessVariables processVariables = ProcessVariables.builder().variables(variables).build();

            getWorkflowEngine().completeTask(taskId, processVariables);
            log.info("Completed task: {}", taskId);
        });
    }


    public List<TaskInfo> getPendingTasks(String userId) {
        log.debug("Getting pending tasks for user: {}", userId);

        ensureAuthentication();

        return WorkflowErrorHandler.executeWithExceptionHandling("getting pending tasks", userId, () -> {
            List<TaskInfo> tasks = getWorkflowEngine().getPendingTasks(userId);
            log.debug("Found {} pending tasks for user: {}", tasks.size(), userId);
            return tasks;
        });
    }


    public List<ProcessInstance> getProcessInstances() {
        log.debug("Getting all process instances");

        ensureAuthentication();

        return WorkflowErrorHandler.executeWithExceptionHandling("getting process instances", "all", () -> {
            List<ProcessInstance> instances = getWorkflowEngine().getProcessInstances();
            log.debug("Found {} active process instances", instances.size());
            return instances;
        });
    }


    public ProcessVariables getProcessVariables(String processInstanceId) {
        log.debug("Getting variables for process instance: {}", processInstanceId);

        ensureAuthentication();

        return WorkflowErrorHandler.executeWithExceptionHandling("getting process variables", processInstanceId, () -> {
            ProcessVariables variables = getWorkflowEngine().getProcessVariables(processInstanceId);
            log.debug("Retrieved {} variables for process instance: {}", variables.getVariables().size(), processInstanceId);
            return variables;
        });
    }

    public ProcessVariables getHistoricProcessVariables(String processInstanceId) {
        log.debug("Getting historic variables for process instance: {}", processInstanceId);

        ensureAuthentication();

        return WorkflowErrorHandler.executeWithExceptionHandling("getting historic process variables", processInstanceId, () -> {
            ProcessVariables variables = getWorkflowEngine().getHistoricProcessVariables(processInstanceId);
            log.debug("Retrieved {} historic variables for process instance: {}", variables.getVariables().size(), processInstanceId);
            return variables;
        });
    }

    public ProcessVariables getTaskVariables(String taskId) {
        log.debug("Getting variables for task: {}", taskId);

        ensureAuthentication();

        return WorkflowErrorHandler.executeWithExceptionHandling("getting task variables", taskId, () -> {
            ProcessVariables variables = getWorkflowEngine().getTaskVariables(taskId);
            log.debug("Retrieved {} variables for task: {}", variables.getVariables().size(), taskId);
            return variables;
        });
    }

    public List<TaskInfo> getPendingTasksForProcess(String processInstanceId) {
        log.debug("Getting pending tasks for process: {}", processInstanceId);

        ensureAuthentication();

        return WorkflowErrorHandler.executeWithExceptionHandling("getting pending tasks for process", processInstanceId, () -> {
            List<TaskInfo> tasks = getWorkflowEngine().getPendingTasksForProcess(processInstanceId);
            log.debug("Found {} pending tasks for process: {}", tasks.size(), processInstanceId);
            return tasks;
        });
    }

    public List<DeploymentInfo> getDeployments() {
        log.debug("Getting all deployments");

        ensureAuthentication();

        return WorkflowErrorHandler.executeWithExceptionHandling("getting deployments", "all", () -> {
            List<DeploymentInfo> deployments = getWorkflowEngine().getDeployments();
            log.debug("Found {} deployments", deployments.size());
            return deployments;
        });
    }


    public List<ProcessDefinition> getProcessDefinitions() {
        log.debug("Getting process definitions");

        ensureAuthentication();

        return WorkflowErrorHandler.executeWithExceptionHandling("getting process definitions", "all", () -> {
            List<ProcessDefinition> definitions = getWorkflowEngine().getProcessDefinitions();
            log.debug("Found {} process definitions", definitions.size());
            return definitions;
        });
    }


    public boolean isEngineActive() {
        return WorkflowErrorHandler.executeWithExceptionHandling("checking engine status", "engine", () -> getWorkflowEngine().isEngineActive());
    }


    public String getEngineType() {
        return WorkflowErrorHandler.executeWithExceptionHandling("getting engine type", "engine", () -> getWorkflowEngine().getEngineType().name());
    }

    public void terminateProcess(String processInstanceId, String reason) {
        log.info("Terminating process instance: {} with reason: {}", processInstanceId, reason);
        ensureAuthentication();
        WorkflowErrorHandler.executeWithExceptionHandling("process termination", processInstanceId, () -> {
            getWorkflowEngine().terminateProcess(processInstanceId, reason);
            log.info("Process instance {} terminated successfully", processInstanceId);
        });
    }

    public ProcessStatus getProcessStatus(String processInstanceId) {
        log.debug("Getting status for process instance: {}", processInstanceId);
        ensureAuthentication();
        return WorkflowErrorHandler.executeWithExceptionHandling("getting process status", processInstanceId, () -> {
            ProcessStatus status = getWorkflowEngine().getProcessStatus(processInstanceId);
            log.debug("Retrieved status for process instance: {}", processInstanceId);
            return status;
        });
    }

    public ProcessCompletionStatus getProcessCompletionStatus(String processInstanceId) {
        log.debug("Getting completion status for process instance: {}", processInstanceId);
        ensureAuthentication();
        return WorkflowErrorHandler.executeWithExceptionHandling("getting process completion status", processInstanceId, () -> {
            ProcessCompletionStatus status = getWorkflowEngine().getProcessCompletionStatus(processInstanceId);
            log.debug("Retrieved completion status for process instance: {}", processInstanceId);
            return status;
        });
    }

    public List<ActiveProcess> getActiveProcesses() {
        log.debug("Getting active processes");
        ensureAuthentication();
        return WorkflowErrorHandler.executeWithExceptionHandling("getting active processes", "all", () -> {
            List<ActiveProcess> processes = getWorkflowEngine().getActiveProcesses();
            log.debug("Found {} active processes", processes.size());
            return processes;
        });
    }

    public List<ProcessDefinitionInfo> getProcessDefinitionsInfo() {
        log.debug("Getting process definitions info");
        ensureAuthentication();
        return WorkflowErrorHandler.executeWithExceptionHandling("getting process definitions info", "all", () -> {
            List<ProcessDefinitionInfo> definitions = getWorkflowEngine().getProcessDefinitionsInfo();
            log.debug("Found {} process definitions", definitions.size());
            return definitions;
        });
    }

    public List<ProcessHistoryInfo> getProcessHistoryInfo() {
        log.debug("Getting process history info");
        ensureAuthentication();
        return WorkflowErrorHandler.executeWithExceptionHandling("getting process history info", "all", () -> {
            List<ProcessHistoryInfo> history = getWorkflowEngine().getProcessHistoryInfo();
            log.debug("Found {} historic process instances", history.size());
            return history;
        });
    }

    public DeploymentInfoEnhanced getDeploymentInfo(String deploymentId) {
        log.debug("Getting deployment info for: {}", deploymentId);
        ensureAuthentication();
        return WorkflowErrorHandler.executeWithExceptionHandling("getting deployment info", deploymentId, () -> {
            DeploymentInfoEnhanced info = getWorkflowEngine().getDeploymentInfo(deploymentId);
            log.debug("Retrieved deployment info for: {}", deploymentId);
            return info;
        });
    }

    public List<DeploymentResource> getDeploymentResources(String deploymentId) {
        log.debug("Getting deployment resources for: {}", deploymentId);
        ensureAuthentication();
        return WorkflowErrorHandler.executeWithExceptionHandling("getting deployment resources", deploymentId, () -> {
            List<DeploymentResource> resources = getWorkflowEngine().getDeploymentResources(deploymentId);
            log.debug("Retrieved {} resources for deployment: {}", resources.size(), deploymentId);
            return resources;
        });
    }

    public byte[] getDeploymentResource(String deploymentId, String resourceName) {
        log.debug("Getting deployment resource: {} from deployment: {}", resourceName, deploymentId);
        ensureAuthentication();
        return WorkflowErrorHandler.executeWithExceptionHandling("getting deployment resource", deploymentId + "/" + resourceName, () -> {
            byte[] resource = getWorkflowEngine().getDeploymentResource(deploymentId, resourceName);
            log.debug("Retrieved resource: {} from deployment: {}", resourceName, deploymentId);
            return resource;
        });
    }

    public void deleteDeployment(String deploymentId) {
        log.info("Deleting deployment: {}", deploymentId);
        ensureAuthentication();
        WorkflowErrorHandler.executeWithExceptionHandling("deployment deletion", deploymentId, () -> {
            getWorkflowEngine().deleteDeployment(deploymentId);
            log.info("Deployment {} deleted successfully", deploymentId);
        });
    }


    private void ensureAuthentication() {
        if (!workflowConfig.getAuthentication().isEnabled()) {
            log.debug("Authentication is disabled, skipping auth check");
            return;
        }

        String cachedAuthKey = fineractAuthService.getCachedAuthKey();
        if (cachedAuthKey == null || cachedAuthKey.isEmpty()) {
            log.error("Authentication required but no cached authentication key found");
            throw new IllegalStateException("Authentication required but no authentication key available. Please authenticate first.");
        } else {
            log.debug("Authentication key available for workflow operations");
        }
    }

}