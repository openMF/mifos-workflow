package org.mifos.workflow.core.engine.delegates;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.mifos.fineract.client.models.PostClientsClientIdResponse;
import org.mifos.workflow.dto.fineract.client.ClientAssignStaffRequestDTO;
import org.mifos.workflow.service.fineract.client.FineractClientService;
import org.mifos.workflow.exception.FineractApiException;
import org.mifos.workflow.exception.WorkflowException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Delegate for assigning staff to a client in the Fineract system.
 * Assigns a loan officer or staff member to the client.
 */
@Component
public class StaffAssignmentDelegate implements JavaDelegate {
    private static final Logger logger = LoggerFactory.getLogger(StaffAssignmentDelegate.class);
    private static final String ASSIGN_STAFF_COMMAND = "assignStaff";
    
    private final FineractClientService fineractClientService;

    @Autowired
    public StaffAssignmentDelegate(FineractClientService fineractClientService) {
        this.fineractClientService = fineractClientService;
    }

    @Override
    public void execute(DelegateExecution execution) {
        logger.info("StaffAssignmentDelegate.execute() called for process instance: {}", execution.getProcessInstanceId());
        try {
            Long clientId = (Long) execution.getVariable("clientId");
            if (clientId == null) {
                throw new IllegalArgumentException("clientId is missing from process variables");
            }
            Object staffIdObj = execution.getVariable("staffId");
            Long staffId = null;
            if (staffIdObj != null) {
                if (staffIdObj instanceof Long) {
                    staffId = (Long) staffIdObj;
                } else if (staffIdObj instanceof Integer) {
                    staffId = ((Integer) staffIdObj).longValue();
                } else if (staffIdObj instanceof String) {
                    try {
                        staffId = Long.parseLong((String) staffIdObj);
                    } catch (NumberFormatException e) {
                        logger.warn("Invalid staffId format: {}", staffIdObj);
                    }
                } else if (staffIdObj instanceof Number) {
                    staffId = ((Number) staffIdObj).longValue();
                }
            }
            if (staffId == null) {
                logger.info("No staffId provided, skipping staff assignment for client: {}", clientId);
                execution.setVariable("staffAssigned", false);
                return;
            }
            Object assignmentDateObj = execution.getVariable("assignmentDate");
            LocalDate assignmentDate = null;
            if (assignmentDateObj != null) {
                if (assignmentDateObj instanceof LocalDate) {
                    assignmentDate = (LocalDate) assignmentDateObj;
                } else if (assignmentDateObj instanceof String) {
                    String assignmentDateStr = (String) assignmentDateObj;
                    if (!assignmentDateStr.trim().isEmpty()) {
                        try {
                            assignmentDate = LocalDate.parse(assignmentDateStr);
                        } catch (Exception e) {
                            logger.warn("Could not parse assignmentDate string: {}, using current date", assignmentDateStr);
                        }
                    }
                } else {
                    logger.warn("Unexpected assignmentDate type: {}, using current date", assignmentDateObj.getClass().getSimpleName());
                }
            }
            if (assignmentDate == null) {
                assignmentDate = LocalDate.now();
            }
            logger.info("Assigning staff {} to client {} on date: {}", staffId, clientId, assignmentDate);
            ClientAssignStaffRequestDTO assignStaffRequest = ClientAssignStaffRequestDTO.builder().staffId(staffId).build();
            PostClientsClientIdResponse response = fineractClientService.assignStaff(clientId, ASSIGN_STAFF_COMMAND, assignStaffRequest).blockingFirst();
            if (response != null && response.getResourceId() != null) {
                execution.setVariable("staffAssigned", true);
                execution.setVariable("assignedStaffId", staffId);
                execution.setVariable("assignmentDate", assignmentDate);
                logger.info("Successfully assigned staff {} to client {}", staffId, clientId);
            } else {
                throw new RuntimeException("Failed to assign staff: No response received");
            }
        } catch (FineractApiException e) {
            if (e.isNotFound() && (e.getErrorBody().contains("staff.id.invalid") || e.getErrorBody().contains("does not exist"))) {
                Object currentStaffIdObj = execution.getVariable("staffId");
                Long currentStaffId = null;
                if (currentStaffIdObj instanceof Long) {
                    currentStaffId = (Long) currentStaffIdObj;
                } else if (currentStaffIdObj instanceof Integer) {
                    currentStaffId = ((Integer) currentStaffIdObj).longValue();
                } else if (currentStaffIdObj instanceof Number) {
                    currentStaffId = ((Number) currentStaffIdObj).longValue();
                }
                logger.warn("Staff with ID {} does not exist. Skipping staff assignment for client: {}", currentStaffId, e.getResourceId());
                execution.setVariable("staffAssigned", false);
                execution.setVariable("staffAssignmentError", "Staff with ID " + currentStaffId + " does not exist");
                execution.setVariable("errorMessage", "Staff assignment skipped - staff not found");
                return;
            }
            logger.error("Fineract API error during staff assignment: {}", e.getMessage());
            execution.setVariable("staffAssigned", false);
            execution.setVariable("errorMessage", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error assigning staff: {}", e.getMessage(), e);
            execution.setVariable("staffAssigned", false);
            execution.setVariable("errorMessage", e.getMessage());
            throw new WorkflowException("Staff assignment failed", e, "staff assignment", "STAFF_ASSIGNMENT_FAILED");
        }
    }
} 