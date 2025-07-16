package org.mifos.workflow.core.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Represents a request to terminate a workflow process instance.
 * Contains information about the termination reason and context.
 */
@Data
@Builder
public class ProcessTerminationRequest {
    private String processInstanceId;
    private String reason;
    private String terminatedBy;
    private LocalDateTime terminationTime;
    private boolean cascade;
    private boolean skipCustomListeners;
    private boolean skipIoMappings;
    private Map<String, Object> terminationVariables;
    private Map<String, Object> metadata;

    public static ProcessTerminationRequest of(String processInstanceId, String reason) {
        return ProcessTerminationRequest.builder().processInstanceId(processInstanceId).reason(reason).terminationTime(LocalDateTime.now()).cascade(false).skipCustomListeners(false).skipIoMappings(false).build();
    }

    public static ProcessTerminationRequest cascade(String processInstanceId, String reason) {
        return ProcessTerminationRequest.builder().processInstanceId(processInstanceId).reason(reason).terminationTime(LocalDateTime.now()).cascade(true).skipCustomListeners(false).skipIoMappings(false).build();
    }
} 