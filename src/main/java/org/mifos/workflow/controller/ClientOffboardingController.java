package org.mifos.workflow.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mifos.workflow.core.model.ActiveProcess;
import org.mifos.workflow.core.model.DeploymentInfo;
import org.mifos.workflow.core.model.DeploymentInfoEnhanced;
import org.mifos.workflow.core.model.DeploymentResource;
import org.mifos.workflow.core.model.DeploymentResult;
import org.mifos.workflow.core.model.ProcessCompletionStatus;
import org.mifos.workflow.core.model.ProcessDefinitionInfo;
import org.mifos.workflow.core.model.ProcessHistoryInfo;
import org.mifos.workflow.core.model.ProcessInstance;
import org.mifos.workflow.core.model.ProcessStatus;
import org.mifos.workflow.core.model.TaskInfo;
import org.mifos.workflow.dto.fineract.client.ClientCloseRequestDTO;
import org.mifos.workflow.service.WorkflowService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for managing client offboarding workflow operations.
 * Provides endpoints for starting, monitoring, and managing client offboarding processes.
 */
@RestController
@RequestMapping("/api/v1/workflow/client-offboarding")
@RequiredArgsConstructor
@Slf4j
public class ClientOffboardingController {

    private final WorkflowService workflowService;

    @PostMapping("/start")
    public ResponseEntity<ProcessInstance> startClientOffboarding(@Valid @RequestBody ClientCloseRequestDTO closeRequest) {
        Long clientId = closeRequest.getClientId(); // Add clientId to DTO for workflow context
        log.info("Starting client offboarding process for client: {}", clientId);
        Map<String, Object> variables = new HashMap<>();
        variables.put("clientId", clientId);
        variables.put("closureReasonId", closeRequest.getClosureReasonId());
        variables.put("closureDate", closeRequest.getClosureDate());
        variables.put("dateFormat", closeRequest.getDateFormat() != null ? closeRequest.getDateFormat() : "yyyy-MM-dd");
        variables.put("locale", closeRequest.getLocale() != null ? closeRequest.getLocale() : "en");
        variables.put("operationsOfficer", "system");
        ProcessInstance processInstance = workflowService.startProcess("client-offboarding", variables);
        log.info("Started client offboarding process: {} for client: {}", processInstance.getId(), clientId);
        return ResponseEntity.ok(processInstance);
    }

    @GetMapping("/tasks")
    public ResponseEntity<List<TaskInfo>> getClientOffboardingTasks() {
        log.info("Retrieving all client offboarding tasks");
        List<TaskInfo> taskInfos = workflowService.getPendingTasks("system");
        return ResponseEntity.ok(taskInfos);
    }

    @PostMapping("/tasks/{taskId}/complete")
    public ResponseEntity<Void> completeOffboardingTask(@PathVariable String taskId, @RequestBody Map<String, Object> taskVariables) {
        log.info("Completing offboarding task: {}", taskId);
        workflowService.completeTask(taskId, taskVariables);
        log.info("Offboarding task {} completed successfully", taskId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/processes/{processInstanceId}/variables")
    public ResponseEntity<Map<String, Object>> getProcessVariables(@PathVariable String processInstanceId) {
        log.info("Retrieving variables for process instance: {}", processInstanceId);
        Map<String, Object> variables = workflowService.getProcessVariables(processInstanceId).getVariables();
        return ResponseEntity.ok(variables);
    }

    @GetMapping("/processes")
    public ResponseEntity<List<ActiveProcess>> getActiveProcesses() {
        log.info("Retrieving active client offboarding processes");
        List<ActiveProcess> activeProcesses = workflowService.getActiveProcesses();
        return ResponseEntity.ok(activeProcesses);
    }

    @GetMapping("/process-definitions")
    public ResponseEntity<List<ProcessDefinitionInfo>> getProcessDefinitions() {
        log.info("Retrieving process definitions");
        List<ProcessDefinitionInfo> definitionInfos = workflowService.getProcessDefinitionsInfo();
        return ResponseEntity.ok(definitionInfos);
    }

    @GetMapping("/history")
    public ResponseEntity<List<ProcessHistoryInfo>> getProcessHistory() {
        log.info("Retrieving process history");
        try {
            List<ProcessHistoryInfo> historyInfos = workflowService.getProcessHistoryInfo();
            log.info("Successfully retrieved {} history records", historyInfos.size());
            return ResponseEntity.ok(historyInfos);
        } catch (Exception e) {
            log.error("Error retrieving process history: {}", e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/history/{processInstanceId}/variables")
    public ResponseEntity<Map<String, Object>> getHistoricProcessVariables(@PathVariable String processInstanceId) {
        log.info("Retrieving historic variables for process instance: {}", processInstanceId);
        Map<String, Object> variableMap = workflowService.getHistoricProcessVariables(processInstanceId).getVariables();
        return ResponseEntity.ok(variableMap);
    }

    @GetMapping("/tasks/{taskId}/variables")
    public ResponseEntity<Map<String, Object>> getTaskVariables(@PathVariable String taskId) {
        log.info("Retrieving variables for task: {}", taskId);
        Map<String, Object> variables = workflowService.getTaskVariables(taskId).getVariables();
        return ResponseEntity.ok(variables);
    }

    @GetMapping("/processes/{processInstanceId}/tasks")
    public ResponseEntity<List<TaskInfo>> getTasksForProcess(@PathVariable String processInstanceId) {
        log.info("Retrieving tasks for process instance: {}", processInstanceId);
        List<TaskInfo> taskInfos = workflowService.getPendingTasksForProcess(processInstanceId);
        return ResponseEntity.ok(taskInfos);
    }

    @DeleteMapping("/processes/{processInstanceId}")
    public ResponseEntity<Void> terminateProcess(@PathVariable String processInstanceId) {
        log.info("Terminating process instance: {}", processInstanceId);
        workflowService.terminateProcess(processInstanceId, "Manual termination via API");
        log.info("Process instance {} terminated successfully", processInstanceId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/processes/{processInstanceId}/status")
    public ResponseEntity<ProcessStatus> getProcessStatus(@PathVariable String processInstanceId) {
        log.info("Getting status for process instance: {}", processInstanceId);
        ProcessStatus status = workflowService.getProcessStatus(processInstanceId);
        return ResponseEntity.ok(status);
    }

    @GetMapping("/processes/{processInstanceId}/completion-status")
    public ResponseEntity<ProcessCompletionStatus> getProcessCompletionStatus(@PathVariable String processInstanceId) {
        log.info("Getting completion status for process instance: {}", processInstanceId);
        ProcessCompletionStatus completionStatus = workflowService.getProcessCompletionStatus(processInstanceId);
        return ResponseEntity.ok(completionStatus);
    }

    @GetMapping("/deployments")
    public ResponseEntity<List<DeploymentInfo>> getDeployments() {
        log.info("Retrieving all deployments");
        List<DeploymentInfo> deployments = workflowService.getDeployments();
        return ResponseEntity.ok(deployments);
    }

    @GetMapping("/deployments/{deploymentId}")
    public ResponseEntity<DeploymentInfoEnhanced> getDeployment(@PathVariable String deploymentId) {
        log.info("Retrieving deployment: {}", deploymentId);
        DeploymentInfoEnhanced deploymentInfo = workflowService.getDeploymentInfo(deploymentId);
        return ResponseEntity.ok(deploymentInfo);
    }

    @GetMapping("/deployments/{deploymentId}/resources")
    public ResponseEntity<List<DeploymentResource>> getDeploymentResources(@PathVariable String deploymentId) {
        log.info("Retrieving resources for deployment: {}", deploymentId);
        List<DeploymentResource> resourceInfos = workflowService.getDeploymentResources(deploymentId);
        return ResponseEntity.ok(resourceInfos);
    }

    @GetMapping("/deployments/{deploymentId}/resources/{resourceName}")
    public ResponseEntity<byte[]> getDeploymentResource(@PathVariable String deploymentId, @PathVariable String resourceName) {
        log.info("Retrieving resource {} from deployment: {}", resourceName, deploymentId);
        byte[] resourceBytes = workflowService.getDeploymentResource(deploymentId, resourceName);
        return ResponseEntity.ok().header("Content-Type", "application/octet-stream").header("Content-Disposition", "attachment; filename=\"" + resourceName + "\"").body(resourceBytes);
    }

    @DeleteMapping("/deployments/{deploymentId}")
    public ResponseEntity<Void> deleteDeployment(@PathVariable String deploymentId) {
        log.info("Deleting deployment: {}", deploymentId);
        workflowService.deleteDeployment(deploymentId);
        log.info("Deployment {} deleted successfully", deploymentId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/deployments")
    public ResponseEntity<DeploymentResult> deployProcess(@RequestParam("file") MultipartFile file) {
        log.info("Deploying process from file: {}", file.getOriginalFilename());
        try (java.io.InputStream inputStream = file.getInputStream()) {
            DeploymentResult result = workflowService.deployProcess(inputStream, file.getOriginalFilename());
            return ResponseEntity.ok(result);
        } catch (java.io.IOException e) {
            throw new RuntimeException("Failed to read uploaded file", e);
        }
    }
} 