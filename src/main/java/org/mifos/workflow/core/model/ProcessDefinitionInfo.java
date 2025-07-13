package org.mifos.workflow.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Represents detailed information about a workflow process definition with comprehensive metadata.
 * This model provides complete information about process definitions including deployment details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessDefinitionInfo {
    
    private String processDefinitionId;
    
    private String processDefinitionKey;
    
    private String processDefinitionName;
    
    private Integer version;
    
    private String deploymentId;
    
    private String deploymentName;
    
    private LocalDateTime deploymentTime;
    
    private String resourceName;
    
    private String diagramResourceName;
    
    private String description;
    
    private Boolean suspended;
    
    private String category;
    
    private Map<String, Object> properties;
    
    private String engineType;
    
    private Integer activeInstances;
    
    private Integer totalInstances;
    
    public static ProcessDefinitionInfo from(Map<String, Object> map) {
        if (map == null) {
            return null;
        }
        
        return ProcessDefinitionInfo.builder()
                .processDefinitionId((String) map.get("processDefinitionId"))
                .processDefinitionKey((String) map.get("processDefinitionKey"))
                .processDefinitionName((String) map.get("processDefinitionName"))
                .version(map.get("version") != null ? 
                    Integer.valueOf(map.get("version").toString()) : null)
                .deploymentId((String) map.get("deploymentId"))
                .deploymentName((String) map.get("deploymentName"))
                .deploymentTime(map.get("deploymentTime") != null ? 
                    parseDateTime(map.get("deploymentTime")) : null)
                .resourceName((String) map.get("resourceName"))
                .diagramResourceName((String) map.get("diagramResourceName"))
                .description((String) map.get("description"))
                .suspended((Boolean) map.get("suspended"))
                .category((String) map.get("category"))
                .properties((Map<String, Object>) map.get("properties"))
                .engineType((String) map.get("engineType"))
                .activeInstances(map.get("activeInstances") != null ? 
                    Integer.valueOf(map.get("activeInstances").toString()) : null)
                .totalInstances(map.get("totalInstances") != null ? 
                    Integer.valueOf(map.get("totalInstances").toString()) : null)
                .build();
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