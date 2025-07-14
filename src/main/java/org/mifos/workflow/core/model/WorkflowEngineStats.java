package org.mifos.workflow.core.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Represents comprehensive statistics and metrics for a workflow engine.
 * Contains information about process instances, tasks, deployments, and performance metrics.
 */
@Data
@Builder
public class WorkflowEngineStats {
    private String engineType;
    private String engineVersion;
    private LocalDateTime statsTime;
    private boolean engineActive;
    private long totalProcessDefinitions;
    private long activeProcessInstances;
    private long completedProcessInstances;
    private long terminatedProcessInstances;
    private long failedProcessInstances;
    private long suspendedProcessInstances;
    private long totalTasks;
    private long pendingTasks;
    private long completedTasks;
    private long assignedTasks;
    private long unassignedTasks;
    private long totalDeployments;
    private long activeDeployments;
    private long suspendedDeployments;
    private long averageProcessDuration;
    private long averageTaskDuration;
    private long totalProcessExecutions;
    private long totalTaskExecutions;
    private long activeUsers;
    private long totalUsers;
    private Map<String, Object> systemMetrics;
    private Map<String, Object> performanceMetrics;
    private Map<String, Object> customMetrics;

    public static WorkflowEngineStats from(Map<String, Object> statsMap) {
        return WorkflowEngineStats.builder()
                .engineType((String) statsMap.get("engineType"))
                .engineVersion((String) statsMap.get("engineVersion"))
                .statsTime(statsMap.get("statsTime") != null ? 
                    LocalDateTime.parse((String) statsMap.get("statsTime")) : LocalDateTime.now())
                .engineActive(statsMap.get("engineActive") != null ? 
                    Boolean.valueOf(statsMap.get("engineActive").toString()) : false)
                .totalProcessDefinitions(statsMap.get("totalProcessDefinitions") != null ? 
                    Long.valueOf(statsMap.get("totalProcessDefinitions").toString()) : 0L)
                .activeProcessInstances(statsMap.get("activeProcessInstances") != null ? 
                    Long.valueOf(statsMap.get("activeProcessInstances").toString()) : 0L)
                .completedProcessInstances(statsMap.get("completedProcessInstances") != null ? 
                    Long.valueOf(statsMap.get("completedProcessInstances").toString()) : 0L)
                .terminatedProcessInstances(statsMap.get("terminatedProcessInstances") != null ? 
                    Long.valueOf(statsMap.get("terminatedProcessInstances").toString()) : 0L)
                .failedProcessInstances(statsMap.get("failedProcessInstances") != null ? 
                    Long.valueOf(statsMap.get("failedProcessInstances").toString()) : 0L)
                .suspendedProcessInstances(statsMap.get("suspendedProcessInstances") != null ? 
                    Long.valueOf(statsMap.get("suspendedProcessInstances").toString()) : 0L)
                .totalTasks(statsMap.get("totalTasks") != null ? 
                    Long.valueOf(statsMap.get("totalTasks").toString()) : 0L)
                .pendingTasks(statsMap.get("pendingTasks") != null ? 
                    Long.valueOf(statsMap.get("pendingTasks").toString()) : 0L)
                .completedTasks(statsMap.get("completedTasks") != null ? 
                    Long.valueOf(statsMap.get("completedTasks").toString()) : 0L)
                .assignedTasks(statsMap.get("assignedTasks") != null ? 
                    Long.valueOf(statsMap.get("assignedTasks").toString()) : 0L)
                .unassignedTasks(statsMap.get("unassignedTasks") != null ? 
                    Long.valueOf(statsMap.get("unassignedTasks").toString()) : 0L)
                .totalDeployments(statsMap.get("totalDeployments") != null ? 
                    Long.valueOf(statsMap.get("totalDeployments").toString()) : 0L)
                .activeDeployments(statsMap.get("activeDeployments") != null ? 
                    Long.valueOf(statsMap.get("activeDeployments").toString()) : 0L)
                .suspendedDeployments(statsMap.get("suspendedDeployments") != null ? 
                    Long.valueOf(statsMap.get("suspendedDeployments").toString()) : 0L)
                .averageProcessDuration(statsMap.get("averageProcessDuration") != null ? 
                    Long.valueOf(statsMap.get("averageProcessDuration").toString()) : 0L)
                .averageTaskDuration(statsMap.get("averageTaskDuration") != null ? 
                    Long.valueOf(statsMap.get("averageTaskDuration").toString()) : 0L)
                .totalProcessExecutions(statsMap.get("totalProcessExecutions") != null ? 
                    Long.valueOf(statsMap.get("totalProcessExecutions").toString()) : 0L)
                .totalTaskExecutions(statsMap.get("totalTaskExecutions") != null ? 
                    Long.valueOf(statsMap.get("totalTaskExecutions").toString()) : 0L)
                .activeUsers(statsMap.get("activeUsers") != null ? 
                    Long.valueOf(statsMap.get("activeUsers").toString()) : 0L)
                .totalUsers(statsMap.get("totalUsers") != null ? 
                    Long.valueOf(statsMap.get("totalUsers").toString()) : 0L)
                .systemMetrics((Map<String, Object>) statsMap.get("systemMetrics"))
                .performanceMetrics((Map<String, Object>) statsMap.get("performanceMetrics"))
                .customMetrics((Map<String, Object>) statsMap.get("customMetrics"))
                .build();
    }
} 