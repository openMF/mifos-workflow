package org.mifos.workflow.core.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Represents historical data for a completed or terminated process instance.
 * Used for auditing and reporting on past workflow executions.
 */
@Data
@Builder
public class HistoricProcessInstance {
    private String id;
    private String processDefinitionId;
    private String businessKey;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long durationInMillis;
    private String outcome;
    private String deleteReason;
    private String startUserId;
}