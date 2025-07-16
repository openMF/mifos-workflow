package org.mifos.workflow.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Represents an active workflow process instance with current execution details.
 * This model provides real-time information about running processes.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActiveProcess {

    private String processInstanceId;

    private String processDefinitionKey;

    private String processDefinitionName;

    private String status;

    private String currentActivityName;

    private String currentActivityId;

    private LocalDateTime startTime;

    private Map<String, Object> variables;

    private String businessKey;

    private Long duration;

    private String startedBy;

    private String assignee;

    public static ActiveProcess from(Map<String, Object> map) {
        if (map == null) {
            return null;
        }

        return ActiveProcess.builder().processInstanceId((String) map.get("processInstanceId")).processDefinitionKey((String) map.get("processDefinitionKey")).processDefinitionName((String) map.get("processDefinitionName")).status((String) map.get("status")).currentActivityName((String) map.get("currentActivityName")).currentActivityId((String) map.get("currentActivityId")).startTime(map.get("startTime") != null ? parseDateTime(map.get("startTime")) : null).variables((Map<String, Object>) map.get("variables")).businessKey((String) map.get("businessKey")).duration(map.get("duration") != null ? Long.valueOf(map.get("duration").toString()) : null).startedBy((String) map.get("startedBy")).assignee((String) map.get("assignee")).build();
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