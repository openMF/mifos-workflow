package org.mifos.workflow.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mifos.workflow.core.model.*;
import org.mifos.workflow.dto.fineract.loan.LoanCancellationRequestDTO;
import org.mifos.workflow.service.WorkflowService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for managing loan cancellation workflows.
 * Provides endpoints for starting and managing loan cancellation processes.
 */
@RestController
@RequestMapping("/api/v1/workflows/loan-cancellation")
@RequiredArgsConstructor
@Slf4j
public class LoanCancellationController {

    private final WorkflowService workflowService;


    @PostMapping("/start")
    public ResponseEntity<ProcessInstance> startLoanCancellation(@Valid @RequestBody LoanCancellationRequestDTO request) {
        log.info("Starting loan cancellation workflow for loan ID: {}", request.getLoanId());

        Map<String, Object> variables = new HashMap<>();
        variables.put("loanId", request.getLoanId());
        variables.put("externalId", request.getExternalId());
        variables.put("cancellationReason", request.getCancellationReason());
        variables.put("cancellationDate", request.getCancellationDate());
        variables.put("cancelledBy", request.getCancelledBy());
        variables.put("notes", request.getNotes());

        variables.put("loanOfficer", request.getLoanOfficer() != null ? request.getLoanOfficer() :
                (request.getCancelledBy() != null ? request.getCancelledBy() : "system"));
        variables.put("assignee", request.getAssignee() != null ? request.getAssignee() : "system");
        variables.put("approver", request.getApprover() != null ? request.getApprover() : "manager");

        ProcessInstance processInstance = workflowService.startProcess("loan-cancellation", variables);

        log.info("Loan cancellation workflow started successfully. Process Instance ID: {}",
                processInstance.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(processInstance);
    }


    @PostMapping("/tasks/{taskId}/complete")
    public ResponseEntity<Void> completeTask(@PathVariable String taskId,
                                             @RequestBody Map<String, Object> variables) {
        log.info("Completing task in loan cancellation workflow. Task ID: {}", taskId);

        List<TaskInfo> allTasks = workflowService.getPendingTasks("system");
        String processInstanceId = null;
        for (TaskInfo task : allTasks) {
            if (task.getTaskId().equals(taskId)) {
                processInstanceId = task.getProcessId();
                break;
            }
        }

        if (processInstanceId != null) {
            workflowService.setProcessVariables(processInstanceId, variables);
            log.info("Set process variables for process instance: {}", processInstanceId);
        }

        workflowService.completeTask(taskId, variables);
        log.info("Task completed successfully. Task ID: {}", taskId);
        return ResponseEntity.ok().build();
    }


    @GetMapping("/processes/{processInstanceId}/tasks")
    public ResponseEntity<List<TaskInfo>> getProcessTasks(@PathVariable String processInstanceId) {
        log.info("Getting tasks for process instance. Process Instance ID: {}", processInstanceId);

        List<TaskInfo> taskInfos = workflowService.getPendingTasksForProcess(processInstanceId);
        return ResponseEntity.ok(taskInfos);
    }


    @GetMapping("/processes/{processInstanceId}/status")
    public ResponseEntity<ProcessStatus> getProcessStatus(@PathVariable String processInstanceId) {
        log.info("Getting status for process instance. Process Instance ID: {}", processInstanceId);

        ProcessStatus processStatus = workflowService.getProcessStatus(processInstanceId);
        return ResponseEntity.ok(processStatus);
    }


    @GetMapping("/processes/{processInstanceId}/variables")
    public ResponseEntity<Map<String, Object>> getProcessVariables(@PathVariable String processInstanceId) {
        log.info("Retrieving variables for process instance: {}", processInstanceId);
        Map<String, Object> variables = workflowService.getProcessVariables(processInstanceId).getVariables();
        return ResponseEntity.ok(variables);
    }


    @PostMapping("/processes/{processInstanceId}/terminate")
    public ResponseEntity<Void> terminateProcess(@PathVariable String processInstanceId,
                                                 @RequestBody ProcessTerminationRequest request) {
        log.info("Terminating process instance. Process Instance ID: {}", processInstanceId);

        workflowService.terminateProcess(processInstanceId, request.getReason());
        log.info("Process instance terminated successfully. Process Instance ID: {}", processInstanceId);
        return ResponseEntity.ok().build();
    }
}
