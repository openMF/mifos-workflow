package org.mifos.workflow.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Represents the current status of a workflow process instance with comprehensive state information.
 * This model provides detailed information about the current state of a process.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessStatus {

    private String processInstanceId;

    private String processDefinitionKey;

    private String processDefinitionName;

    private String status;

    private String currentActivityName;

    private String currentActivityId;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Map<String, Object> variables;

    private String businessKey;

    private Long duration;

    private String startedBy;

    private String assignee;

    private Boolean suspended;

    private Boolean ended;

    public static ProcessStatus from(Map<String, Object> map) {
        if (map == null) {
            return null;
        }

        return ProcessStatus.builder().processInstanceId((String) map.get("processInstanceId")).processDefinitionKey((String) map.get("processDefinitionKey")).processDefinitionName((String) map.get("processDefinitionName")).status((String) map.get("status")).currentActivityName((String) map.get("currentActivityName")).currentActivityId((String) map.get("currentActivityId")).startTime(map.get("startTime") != null ? parseDateTime(map.get("startTime")) : null).endTime(map.get("endTime") != null ? parseDateTime(map.get("endTime")) : null).variables((Map<String, Object>) map.get("variables")).businessKey((String) map.get("businessKey")).duration(map.get("duration") != null ? Long.valueOf(map.get("duration").toString()) : null).startedBy((String) map.get("startedBy")).assignee((String) map.get("assignee")).suspended((Boolean) map.get("suspended")).ended((Boolean) map.get("ended")).build();
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