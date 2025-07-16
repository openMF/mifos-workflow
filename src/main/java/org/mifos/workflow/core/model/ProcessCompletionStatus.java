package org.mifos.workflow.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Represents the completion status of a workflow process instance with final outcomes and variables.
 * This model provides information about how a process ended and its final state.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessCompletionStatus {

    private String processInstanceId;

    private String processDefinitionKey;

    private String processDefinitionName;

    private String outcome;

    private String completionReason;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Map<String, Object> variables;

    private String businessKey;

    private Long duration;

    private String startedBy;

    private String completedBy;

    private String errorMessage;

    private String stackTrace;

    private Boolean successful;

    public static ProcessCompletionStatus from(Map<String, Object> map) {
        if (map == null) {
            return null;
        }

        return ProcessCompletionStatus.builder().processInstanceId((String) map.get("processInstanceId")).processDefinitionKey((String) map.get("processDefinitionKey")).processDefinitionName((String) map.get("processDefinitionName")).outcome((String) map.get("outcome")).completionReason((String) map.get("completionReason")).startTime(map.get("startTime") != null ? parseDateTime(map.get("startTime")) : null).endTime(map.get("endTime") != null ? parseDateTime(map.get("endTime")) : null).variables((Map<String, Object>) map.get("variables")).businessKey((String) map.get("businessKey")).duration(map.get("duration") != null ? Long.valueOf(map.get("duration").toString()) : null).startedBy((String) map.get("startedBy")).completedBy((String) map.get("completedBy")).errorMessage((String) map.get("errorMessage")).stackTrace((String) map.get("stackTrace")).successful((Boolean) map.get("successful")).build();
    }

    private static LocalDateTime parseDateTime(Object dateTimeObj) {
        if (dateTimeObj == null) {
            return null;
        }

        String dateTimeStr = dateTimeObj.toString();
        if (dateTimeStr.isEmpty()) {
            return null;
        }

        try {
            return LocalDateTime.parse(dateTimeStr);
        } catch (Exception e) {
            try {
                return LocalDateTime.parse(dateTimeStr.replace(" ", "T"));
            } catch (Exception e2) {
                return null;
            }
        }
    }
} 