package org.mifos.workflow.core.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Represents a request to replay a workflow process instance.
 * Contains information about the replay context and variables.
 */
@Data
@Builder
public class ProcessReplayRequest {
    private String processInstanceId;
    private ProcessVariables variables;
    private String replayedBy;
    private LocalDateTime replayTime;
    private boolean skipCustomListeners;
    private boolean skipIoMappings;
    private String replayReason;
    private Map<String, Object> replayContext;
    private Map<String, Object> metadata;

    public static ProcessReplayRequest of(String processInstanceId, ProcessVariables variables) {
        return ProcessReplayRequest.builder().processInstanceId(processInstanceId).variables(variables).replayTime(LocalDateTime.now()).skipCustomListeners(false).skipIoMappings(false).build();
    }

    public static ProcessReplayRequest of(String processInstanceId, ProcessVariables variables, String replayReason) {
        return ProcessReplayRequest.builder().processInstanceId(processInstanceId).variables(variables).replayReason(replayReason).replayTime(LocalDateTime.now()).skipCustomListeners(false).skipIoMappings(false).build();
    }
} 