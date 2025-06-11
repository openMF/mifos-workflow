package org.mifos.workflow.core.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Represents a running instance of a workflow process.
 * Used to track active processes in the system.
 */
@Data
@Builder
public class ProcessInstance {
    private String id;
    private String processDefinitionId;
    private String businessKey;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String startUserId;
}