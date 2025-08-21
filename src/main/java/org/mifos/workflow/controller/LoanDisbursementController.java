package org.mifos.workflow.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mifos.workflow.core.model.*;
import org.mifos.workflow.dto.fineract.loan.LoanDisbursementRequestDTO;
import org.mifos.workflow.service.WorkflowService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for managing loan disbursement workflow operations.
 * Provides endpoints for starting, monitoring, and managing loan disbursement processes.
 */
@RestController
@RequestMapping("/api/v1/workflow/loan-disbursement")
@RequiredArgsConstructor
@Slf4j
public class LoanDisbursementController {

    private final WorkflowService workflowService;

    @PostMapping("/start")
    public ResponseEntity<ProcessInstance> startLoanDisbursement(@Valid @RequestBody LoanDisbursementRequestDTO disbursementRequest) {
        log.info("Starting loan disbursement process for loan: {}", disbursementRequest.getLoanId());

        Map<String, Object> variables = new HashMap<>();

        variables.put("loanId", disbursementRequest.getLoanId());
        variables.put("externalId", disbursementRequest.getExternalId());
        variables.put("actualDisbursementDate", disbursementRequest.getActualDisbursementDate());
        variables.put("transactionAmount", disbursementRequest.getTransactionAmount());
        variables.put("disbursementMethod", disbursementRequest.getDisbursementMethod());
        variables.put("accountNumber", disbursementRequest.getAccountNumber());
        variables.put("note", disbursementRequest.getNote());
        variables.put("requestNotes", disbursementRequest.getRequestNotes());
        variables.put("dateFormat", disbursementRequest.getDateFormat() != null ? disbursementRequest.getDateFormat() : "yyyy-MM-dd");
        variables.put("locale", disbursementRequest.getLocale() != null ? disbursementRequest.getLocale() : "en");

        variables.put("disbursementOfficer", disbursementRequest.getDisbursementOfficer() != null ? disbursementRequest.getDisbursementOfficer() : "system");
        variables.put("manager", disbursementRequest.getManager() != null ? disbursementRequest.getManager() : "system");
        variables.put("itSupport", disbursementRequest.getItSupport() != null ? disbursementRequest.getItSupport() : "system");

        variables.put("complianceCheck", disbursementRequest.getComplianceCheck() != null ? disbursementRequest.getComplianceCheck() : false);
        variables.put("complianceNotes", disbursementRequest.getComplianceNotes());
        variables.put("requiresManagerApproval", disbursementRequest.getRequiresManagerApproval() != null ? disbursementRequest.getRequiresManagerApproval() : true);
        variables.put("requiresComplianceReview", disbursementRequest.getRequiresComplianceReview() != null ? disbursementRequest.getRequiresComplianceReview() : false);

        variables.put("clientNotificationMethod", disbursementRequest.getClientNotificationMethod());
        variables.put("requireClientAcknowledgement", disbursementRequest.getRequireClientAcknowledgement() != null ? disbursementRequest.getRequireClientAcknowledgement() : false);

        variables.put("priority", disbursementRequest.getPriority() != null ? disbursementRequest.getPriority() : "NORMAL");
        variables.put("riskLevel", disbursementRequest.getRiskLevel() != null ? disbursementRequest.getRiskLevel() : "LOW");
        variables.put("isUrgent", disbursementRequest.getIsUrgent() != null ? disbursementRequest.getIsUrgent() : false);
        variables.put("urgencyReason", disbursementRequest.getUrgencyReason());

        variables.put("disbursementChannel", disbursementRequest.getDisbursementChannel());
        variables.put("bankCode", disbursementRequest.getBankCode());
        variables.put("branchCode", disbursementRequest.getBranchCode());
        variables.put("referenceNumber", disbursementRequest.getReferenceNumber());

        variables.put("beneficiaryName", disbursementRequest.getBeneficiaryName());
        variables.put("beneficiaryId", disbursementRequest.getBeneficiaryId());
        variables.put("beneficiaryPhone", disbursementRequest.getBeneficiaryPhone());
        variables.put("beneficiaryEmail", disbursementRequest.getBeneficiaryEmail());

        variables.put("destinationAccount", disbursementRequest.getDestinationAccount());
        variables.put("destinationBank", disbursementRequest.getDestinationBank());
        variables.put("destinationBranch", disbursementRequest.getDestinationBranch());

        variables.put("currencyCode", disbursementRequest.getCurrencyCode());
        variables.put("exchangeRate", disbursementRequest.getExchangeRate());
        variables.put("sourceOfFunds", disbursementRequest.getSourceOfFunds());
        variables.put("purpose", disbursementRequest.getPurpose());

        variables.put("autoRetryOnFailure", disbursementRequest.getAutoRetryOnFailure() != null ? disbursementRequest.getAutoRetryOnFailure() : true);
        variables.put("maxRetryAttempts", disbursementRequest.getMaxRetryAttempts() != null ? disbursementRequest.getMaxRetryAttempts() : 3);
        variables.put("escalationLevel", disbursementRequest.getEscalationLevel() != null ? disbursementRequest.getEscalationLevel() : "LEVEL1");

        variables.put("processVersion", "2.0");
        variables.put("correlationId", disbursementRequest.getCorrelationId());
        variables.put("expectedCompletionDate", disbursementRequest.getExpectedCompletionDate());
        variables.put("createdBy", disbursementRequest.getCreatedBy() != null ? disbursementRequest.getCreatedBy() : "system");
        variables.put("createdDate", disbursementRequest.getCreatedDate() != null ? disbursementRequest.getCreatedDate() : LocalDate.now());

        if (disbursementRequest.getDisbursementData() != null) {
            variables.put("disbursementData", disbursementRequest.getDisbursementData());
        }

        if (disbursementRequest.getAdditionalProperties() != null) {
            variables.putAll(disbursementRequest.getAdditionalProperties());
        }

        if (disbursementRequest.getMetadata() != null) {
            variables.put("metadata", disbursementRequest.getMetadata());
        }

        if (disbursementRequest.getAttachments() != null) {
            variables.put("attachments", disbursementRequest.getAttachments());
        }

        variables.put("assignee", "system");

        ProcessInstance processInstance = workflowService.startProcess("loan-disbursement", variables);

        log.info("Started loan disbursement process: {} for loan: {}",
                processInstance.getId(), disbursementRequest.getLoanId());
        return ResponseEntity.ok(processInstance);
    }

    @PostMapping("/retry/{processInstanceId}")
    public ResponseEntity<ProcessInstance> retryDisbursement(@PathVariable String processInstanceId,
                                                             @RequestBody Map<String, Object> retryVariables) {
        log.info("Retrying loan disbursement for process instance: {}", processInstanceId);

        retryVariables.put("retryAttempt", getRetryAttempt(processInstanceId) + 1);
        retryVariables.put("retryDate", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        retryVariables.put("lastRetryBy", retryVariables.getOrDefault("retryBy", "system"));

        workflowService.setProcessVariables(processInstanceId, retryVariables);

        List<TaskInfo> tasks = workflowService.getPendingTasksForProcess(processInstanceId);
        if (!tasks.isEmpty()) {
            workflowService.completeTask(tasks.get(0).getTaskId(), retryVariables);
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping("/escalate/{processInstanceId}")
    public ResponseEntity<Void> escalateDisbursement(@PathVariable String processInstanceId,
                                                     @RequestBody Map<String, Object> escalationVariables) {
        log.info("Escalating loan disbursement for process instance: {}", processInstanceId);

        escalationVariables.put("escalated", true);
        escalationVariables.put("escalationDate", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        escalationVariables.put("escalatedBy", escalationVariables.getOrDefault("escalatedBy", "system"));

        workflowService.setProcessVariables(processInstanceId, escalationVariables);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/approve/{processInstanceId}")
    public ResponseEntity<Void> approveDisbursement(@PathVariable String processInstanceId,
                                                    @RequestBody Map<String, Object> approvalVariables) {
        log.info("Approving loan disbursement for process instance: {}", processInstanceId);

        approvalVariables.put("managerApproved", true);
        approvalVariables.put("approvedDate", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        approvalVariables.put("approvedBy", approvalVariables.getOrDefault("approvedBy", "system"));

        workflowService.setProcessVariables(processInstanceId, approvalVariables);

        List<TaskInfo> tasks = workflowService.getPendingTasksForProcess(processInstanceId);
        for (TaskInfo task : tasks) {
            if (task.getName().contains("Manager Review")) {
                workflowService.completeTask(task.getTaskId(), approvalVariables);
                break;
            }
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping("/reject/{processInstanceId}")
    public ResponseEntity<Void> rejectDisbursement(@PathVariable String processInstanceId,
                                                   @RequestBody Map<String, Object> rejectionVariables) {
        log.info("Rejecting loan disbursement for process instance: {}", processInstanceId);

        rejectionVariables.put("managerApproved", false);
        rejectionVariables.put("rejectedDate", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        rejectionVariables.put("rejectedBy", rejectionVariables.getOrDefault("rejectedBy", "system"));

        workflowService.setProcessVariables(processInstanceId, rejectionVariables);

        List<TaskInfo> tasks = workflowService.getPendingTasksForProcess(processInstanceId);
        for (TaskInfo task : tasks) {
            if (task.getName().contains("Manager Review")) {
                workflowService.completeTask(task.getTaskId(), rejectionVariables);
                break;
            }
        }

        return ResponseEntity.ok().build();
    }

    @GetMapping("/tasks")
    public ResponseEntity<List<TaskInfo>> getLoanDisbursementTasks() {
        log.info("Retrieving all loan disbursement tasks");
        List<TaskInfo> taskInfos = workflowService.getPendingTasks("system");
        return ResponseEntity.ok(taskInfos);
    }

    @GetMapping("/tasks/by-role/{role}")
    public ResponseEntity<List<TaskInfo>> getTasksByRole(@PathVariable String role) {
        log.info("Retrieving loan disbursement tasks for role: {}", role);
        List<TaskInfo> taskInfos = workflowService.getPendingTasks(role);
        return ResponseEntity.ok(taskInfos);
    }

    @PostMapping("/tasks/{taskId}/complete")
    public ResponseEntity<Void> completeTask(@PathVariable String taskId, @RequestBody Map<String, Object> taskVariables) {
        log.info("Completing task: {} with variables: {}", taskId, taskVariables);
        workflowService.completeTask(taskId, taskVariables);
        log.info("Task {} completed successfully", taskId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/processes/{processInstanceId}/variables")
    public ResponseEntity<Map<String, Object>> getProcessVariables(@PathVariable String processInstanceId) {
        log.info("Retrieving variables for process instance: {}", processInstanceId);
        Map<String, Object> variables = workflowService.getProcessVariables(processInstanceId).getVariables();
        return ResponseEntity.ok(variables);
    }

    @PostMapping("/processes/{processInstanceId}/variables")
    public ResponseEntity<Void> setProcessVariables(@PathVariable String processInstanceId, @RequestBody Map<String, Object> variables) {
        log.info("Setting variables for process instance: {} with variables: {}", processInstanceId, variables);
        workflowService.setProcessVariables(processInstanceId, variables);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/processes")
    public ResponseEntity<List<ActiveProcess>> getActiveProcesses() {
        log.info("Retrieving active loan disbursement processes");
        List<ActiveProcess> activeProcesses = workflowService.getActiveProcesses();
        return ResponseEntity.ok(activeProcesses);
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

    @GetMapping("/processes/{processInstanceId}/history")
    public ResponseEntity<List<ProcessHistoryInfo>> getProcessHistory(@PathVariable String processInstanceId) {
        log.info("Getting history for process instance: {}", processInstanceId);
        List<ProcessHistoryInfo> history = workflowService.getProcessHistoryInfo();
        return ResponseEntity.ok(history);
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getDisbursementStatistics() {
        log.info("Getting loan disbursement statistics");

        Map<String, Object> statistics = new HashMap<>();
        List<ActiveProcess> activeProcesses = workflowService.getActiveProcesses();

        long totalActive = activeProcesses.size();
        long pendingApproval = activeProcesses.stream()
                .filter(p -> p.getProcessDefinitionName().contains("loan-disbursement"))
                .count();

        statistics.put("totalActiveProcesses", totalActive);
        statistics.put("pendingApproval", pendingApproval);
        statistics.put("completedToday", 0);
        statistics.put("failedToday", 0);

        return ResponseEntity.ok(statistics);
    }

    private int getRetryAttempt(String processInstanceId) {
        try {
            Map<String, Object> variables = workflowService.getProcessVariables(processInstanceId).getVariables();
            Object retryAttempt = variables.get("retryAttempt");
            return retryAttempt != null ? (Integer) retryAttempt : 0;
        } catch (Exception e) {
            log.warn("Could not get retry attempt for process instance: {}", processInstanceId, e);
            return 0;
        }
    }
}

