package org.mifos.workflow.core.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a resource file associated with a workflow deployment.
 * Contains information about BPMN files, diagrams, and other deployment artifacts.
 */
@Data
@Builder
public class DeploymentResource {
    private String id;
    private String name;
    private String deploymentId;
    private String description;
    private String key;
    private String category;
    private String tenantId;
    private String engineVersion;
    private LocalDateTime deploymentTime;
    private String resourceType;
    private Long resourceSize;
    private String checksum;
    private Map<String, Object> properties;
    private Map<String, Object> metadata;

    public static DeploymentResource from(Map<String, Object> resourceMap) {
        return DeploymentResource.builder().id((String) resourceMap.get("id")).name((String) resourceMap.get("name")).deploymentId((String) resourceMap.get("deploymentId")).description((String) resourceMap.get("description")).key((String) resourceMap.get("key")).category((String) resourceMap.get("category")).tenantId((String) resourceMap.get("tenantId")).engineVersion((String) resourceMap.get("engineVersion")).deploymentTime(resourceMap.get("deploymentTime") != null ? parseDateTime(resourceMap.get("deploymentTime")) : null).resourceType((String) resourceMap.get("resourceType")).resourceSize(resourceMap.get("resourceSize") != null ? Long.valueOf(resourceMap.get("resourceSize").toString()) : null).checksum((String) resourceMap.get("checksum")).properties((Map<String, Object>) resourceMap.get("properties")).metadata((Map<String, Object>) resourceMap.get("metadata")).build();
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("name", name);
        map.put("deploymentId", deploymentId);
        map.put("description", description);
        map.put("key", key);
        map.put("category", category);
        map.put("tenantId", tenantId);
        map.put("engineVersion", engineVersion);
        map.put("deploymentTime", deploymentTime != null ? deploymentTime.toString() : null);
        map.put("resourceType", resourceType);
        map.put("resourceSize", resourceSize);
        map.put("checksum", checksum);
        map.put("properties", properties);
        map.put("metadata", metadata);
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