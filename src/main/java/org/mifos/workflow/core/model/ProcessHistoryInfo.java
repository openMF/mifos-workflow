package org.mifos.workflow.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Represents historical information about workflow process instances for analytics and auditing.
 * This model provides complete process lifecycle tracking and audit trail information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessHistoryInfo {

    private String historicProcessInstanceId;

    private String processInstanceId;

    private String processDefinitionKey;

    private String processDefinitionName;

    private Integer processDefinitionVersion;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Long duration;

    private String startedBy;

    private String completedBy;

    private String status;

    private String completionReason;

    private String businessKey;

    private Map<String, Object> variables;

    private String deploymentId;

    private Boolean successful;

    private String errorMessage;

    private String stackTrace;

    private String category;

    private String description;

    public static ProcessHistoryInfo from(Map<String, Object> map) {
        if (map == null) {
            return null;
        }

        return ProcessHistoryInfo.builder().historicProcessInstanceId((String) map.get("historicProcessInstanceId")).processInstanceId((String) map.get("processInstanceId")).processDefinitionKey((String) map.get("processDefinitionKey")).processDefinitionName((String) map.get("processDefinitionName")).processDefinitionVersion(map.get("processDefinitionVersion") != null ? Integer.valueOf(map.get("processDefinitionVersion").toString()) : null).startTime(map.get("startTime") != null ? parseDateTime(map.get("startTime")) : null).endTime(map.get("endTime") != null ? parseDateTime(map.get("endTime")) : null).duration(map.get("duration") != null ? Long.valueOf(map.get("duration").toString()) : null).startedBy((String) map.get("startedBy")).completedBy((String) map.get("completedBy")).status((String) map.get("status")).completionReason((String) map.get("completionReason")).businessKey((String) map.get("businessKey")).variables((Map<String, Object>) map.get("variables")).deploymentId((String) map.get("deploymentId")).successful((Boolean) map.get("successful")).errorMessage((String) map.get("errorMessage")).stackTrace((String) map.get("stackTrace")).category((String) map.get("category")).description((String) map.get("description")).build();
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