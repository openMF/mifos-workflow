package org.mifos.workflow.core.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents enhanced information about a workflow deployment.
 * Contains comprehensive deployment metadata including resources and configuration.
 */
@Data
@Builder
public class DeploymentInfoEnhanced {
    private String id;
    private String name;
    private String description;
    private String category;
    private String tenantId;
    private LocalDateTime deploymentTime;
    private String engineVersion;
    private boolean suspended;
    private Map<String, Object> properties;
    private Map<String, Object> metadata;
    private Map<String, Object> deploymentData;
    private Map<String, Object> configuration;

    public static DeploymentInfoEnhanced from(Map<String, Object> deploymentMap) {
        return DeploymentInfoEnhanced.builder().id((String) deploymentMap.get("id")).name((String) deploymentMap.get("name")).description((String) deploymentMap.get("description")).category((String) deploymentMap.get("category")).tenantId((String) deploymentMap.get("tenantId")).deploymentTime(deploymentMap.get("deploymentTime") != null ? parseDateTime(deploymentMap.get("deploymentTime")) : null).engineVersion((String) deploymentMap.get("engineVersion")).suspended(deploymentMap.get("suspended") != null ? Boolean.valueOf(deploymentMap.get("suspended").toString()) : false).properties((Map<String, Object>) deploymentMap.get("properties")).metadata((Map<String, Object>) deploymentMap.get("metadata")).deploymentData((Map<String, Object>) deploymentMap.get("deploymentData")).configuration((Map<String, Object>) deploymentMap.get("configuration")).build();
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("name", name);
        map.put("description", description);
        map.put("category", category);
        map.put("tenantId", tenantId);
        map.put("deploymentTime", deploymentTime != null ? deploymentTime.toString() : null);
        map.put("engineVersion", engineVersion);
        map.put("suspended", suspended);
        map.put("properties", properties);
        map.put("metadata", metadata);
        map.put("deploymentData", deploymentData);
        map.put("configuration", configuration);
        return map;
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