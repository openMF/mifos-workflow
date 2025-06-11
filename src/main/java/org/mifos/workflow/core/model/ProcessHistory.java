package org.mifos.workflow.core.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Represents the historical data of a process instance, including activities and transitions.
 * Used for auditing and debugging workflow executions.
 */
@Data
@Builder
public class ProcessHistory {
    private String historyId;
    private String processId;
    private String processDefinitionId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long durationInMillis;
    private String startUserId;
    private String startActivityId;
    private String deleteReason;
    private ProcessVariables variables;
}