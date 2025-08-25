package org.mifos.workflow.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mifos.workflow.core.model.*;
import org.mifos.workflow.dto.fineract.loan.LoanCreateRequestDTO;
import org.mifos.workflow.dto.fineract.loan.LoanApprovalRequestDTO;
import org.mifos.workflow.dto.fineract.loan.LoanRejectionRequestDTO;
import org.mifos.workflow.service.WorkflowService;
import org.mifos.workflow.util.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for managing loan origination workflow operations.
 * Provides endpoints for starting, monitoring, and managing loan origination processes.
 */
@RestController
@RequestMapping("/api/v1/workflow/loan-origination")
@RequiredArgsConstructor
@Slf4j
public class LoanOriginationController {

    private final WorkflowService workflowService;

    @PostMapping("/start")
    public ResponseEntity<ProcessInstance> startLoanOrigination(@Valid @RequestBody LoanCreateRequestDTO loanRequest) {
        log.info("Starting loan origination process for client: {}, product: {}", loanRequest.getClientId(), loanRequest.getProductId());

        Map<String, Object> variables = new HashMap<>();
        variables.put("clientId", loanRequest.getClientId());
        variables.put("groupId", loanRequest.getGroupId());
        variables.put("productId", loanRequest.getProductId());
        variables.put("principal", loanRequest.getPrincipal());
        variables.put("loanTermFrequency", loanRequest.getLoanTermFrequency());
        variables.put("loanTermFrequencyType", loanRequest.getLoanTermFrequencyType());
        variables.put("loanType", loanRequest.getLoanType());
        variables.put("loanPurposeId", loanRequest.getLoanPurposeId());
        variables.put("interestRatePerPeriod", loanRequest.getInterestRatePerPeriod());
        variables.put("interestRateFrequencyType", loanRequest.getInterestRateFrequencyType());
        variables.put("amortizationType", loanRequest.getAmortizationType());
        variables.put("interestCalculationPeriodType", loanRequest.getInterestCalculationPeriodType());
        variables.put("transactionProcessingStrategyCode", loanRequest.getTransactionProcessingStrategyId());
        variables.put("loanDate", loanRequest.getLoanDate());
        variables.put("submittedOnDate", loanRequest.getSubmittedOnDate());
        variables.put("externalId", loanRequest.getExternalId());
        variables.put("dateFormat", loanRequest.getDateFormat() != null ? loanRequest.getDateFormat() : "yyyy-MM-dd");
        variables.put("locale", loanRequest.getLocale() != null ? loanRequest.getLocale() : "en");

        if (loanRequest.getAdditionalProperties() != null) {
            Map<String, Object> processedProperties = new HashMap<>();
            for (Map.Entry<String, Object> entry : loanRequest.getAdditionalProperties().entrySet()) {
                Object value = entry.getValue();
                if (value instanceof String && ((String) value).contains(",")) {
                    String strValue = (String) value;
                    if (strValue.matches("^[0-9,]+(\\.[0-9]+)?$")) {
                        processedProperties.put(entry.getKey(), strValue.replace(",", ""));
                    } else {
                        processedProperties.put(entry.getKey(), value);
                    }
                } else {
                    processedProperties.put(entry.getKey(), value);
                }
            }
            variables.putAll(processedProperties);
        }

        if (loanRequest.getDisbursementData() != null) {
            variables.put("disbursementData", loanRequest.getDisbursementData());
        }

        if (loanRequest.getCharges() != null) {
            variables.put("charges", loanRequest.getCharges());
        }

        if (loanRequest.getCollateral() != null) {
            variables.put("collateral", loanRequest.getCollateral());
        }

        if (loanRequest.getGuarantors() != null) {
            variables.put("guarantors", loanRequest.getGuarantors());
        }

        variables.put("assignee", "system");
        variables.put("loanOfficer", "system");
        variables.put("approver", "system");

        ProcessInstance processInstance = workflowService.startProcess("loan-origination", variables);

        log.info("Started loan origination process: {} for client: {}, product: {}", processInstance.getId(), loanRequest.getClientId(), loanRequest.getProductId());
        return ResponseEntity.ok(processInstance);
    }

    @PostMapping("/approve")
    public ResponseEntity<ProcessInstance> approveLoan(@Valid @RequestBody LoanApprovalRequestDTO approvalRequest) {
        log.info("Starting loan approval process for loan: {}", approvalRequest.getLoanId());

        Map<String, Object> variables = new HashMap<>();
        variables.put("loanId", approvalRequest.getLoanId());
        variables.put("externalId", approvalRequest.getExternalId());
        variables.put("approvedOnDate", approvalRequest.getApprovedOnDate());
        variables.put("approvedByUsername", approvalRequest.getApprovedByUsername());
        variables.put("note", approvalRequest.getNote());
        variables.put("dateFormat", approvalRequest.getDateFormat() != null ? approvalRequest.getDateFormat() : "yyyy-MM-dd");
        variables.put("locale", approvalRequest.getLocale() != null ? approvalRequest.getLocale() : "en");

        if (approvalRequest.getAdditionalProperties() != null) {
            variables.putAll(approvalRequest.getAdditionalProperties());
        }

        variables.put("assignee", "system");
        variables.put("approver", "system");

        ProcessInstance processInstance = workflowService.startProcess("loan-approval", variables);

        log.info("Started loan approval process: {} for loan: {}", processInstance.getId(), approvalRequest.getLoanId());
        return ResponseEntity.ok(processInstance);
    }

    @PostMapping("/reject")
    public ResponseEntity<ProcessInstance> rejectLoan(@Valid @RequestBody LoanRejectionRequestDTO rejectionRequest) {
        log.info("Starting loan rejection process for loan: {}", rejectionRequest.getLoanId());

        Map<String, Object> variables = new HashMap<>();
        variables.put("loanId", rejectionRequest.getLoanId());
        variables.put("externalId", rejectionRequest.getExternalId());
        variables.put("rejectedOnDate", rejectionRequest.getRejectedOnDate());
        variables.put("rejectedReasonId", rejectionRequest.getRejectedReasonId());
        variables.put("rejectedByUsername", rejectionRequest.getRejectedByUsername());
        variables.put("note", rejectionRequest.getNote());
        variables.put("dateFormat", rejectionRequest.getDateFormat() != null ? rejectionRequest.getDateFormat() : "yyyy-MM-dd");
        variables.put("locale", rejectionRequest.getLocale() != null ? rejectionRequest.getLocale() : "en");

        if (rejectionRequest.getAdditionalProperties() != null) {
            variables.putAll(rejectionRequest.getAdditionalProperties());
        }

        variables.put("assignee", "system");
        variables.put("rejector", "system");

        ProcessInstance processInstance = workflowService.startProcess("loan-rejection", variables);

        log.info("Started loan rejection process: {} for loan: {}", processInstance.getId(), rejectionRequest.getLoanId());
        return ResponseEntity.ok(processInstance);
    }

    @GetMapping("/tasks")
    public ResponseEntity<List<TaskInfo>> getLoanOriginationTasks() {
        log.info("Retrieving all loan origination tasks");
        List<TaskInfo> taskInfos = workflowService.getPendingTasks("system");
        return ResponseEntity.ok(taskInfos);
    }

    @PostMapping("/tasks/{taskId}/complete")
    public ResponseEntity<ApiResponse<Void>> completeTask(@PathVariable String taskId, @RequestBody Map<String, Object> taskVariables) {
        log.info("Completing task: {} with variables: {}", taskId, taskVariables);

        if (taskVariables.containsKey("loanType")) {
            Object loanTypeValue = taskVariables.get("loanType");
            log.info("loanType in task variables: {} (type: {})", loanTypeValue, loanTypeValue != null ? loanTypeValue.getClass().getSimpleName() : "null");
        } else {
            log.warn("loanType not found in task variables");
        }

        workflowService.completeTask(taskId, taskVariables);
        log.info("Task {} completed successfully", taskId);
        return ResponseEntity.ok(ApiResponse.success("Task completed successfully"));
    }

    @GetMapping("/processes/{processInstanceId}/variables")
    public ResponseEntity<Map<String, Object>> getProcessVariables(@PathVariable String processInstanceId) {
        log.info("Retrieving variables for process instance: {}", processInstanceId);
        Map<String, Object> variables = workflowService.getProcessVariables(processInstanceId).getVariables();
        return ResponseEntity.ok(variables);
    }

    @GetMapping("/processes")
    public ResponseEntity<List<ActiveProcess>> getActiveProcesses() {
        log.info("Retrieving active loan origination processes");
        List<ActiveProcess> activeProcesses = workflowService.getActiveProcesses();
        return ResponseEntity.ok(activeProcesses);
    }

    @GetMapping("/processes/{processInstanceId}/tasks")
    public ResponseEntity<List<TaskInfo>> getTasksForProcess(@PathVariable String processInstanceId) {
        log.info("Retrieving tasks for process instance: {}", processInstanceId);
        List<TaskInfo> taskInfos = workflowService.getPendingTasksForProcess(processInstanceId);
        return ResponseEntity.ok(taskInfos);
    }

    @PostMapping("/processes/{processInstanceId}/variables")
    public ResponseEntity<ApiResponse<Void>> setProcessVariables(@PathVariable String processInstanceId, @RequestBody Map<String, Object> variables) {
        log.info("Setting variables for process instance: {} with variables: {}", processInstanceId, variables);
        workflowService.setProcessVariables(processInstanceId, variables);
        return ResponseEntity.ok(ApiResponse.success("Process variables updated"));
    }

    @DeleteMapping("/processes/{processInstanceId}")
    public ResponseEntity<ApiResponse<Void>> terminateProcess(@PathVariable String processInstanceId) {
        log.info("Terminating process instance: {}", processInstanceId);
        workflowService.terminateProcess(processInstanceId, "Manual termination via API");
        log.info("Process instance {} terminated successfully", processInstanceId);
        return ResponseEntity.ok(ApiResponse.success("Process terminated successfully"));
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
}

