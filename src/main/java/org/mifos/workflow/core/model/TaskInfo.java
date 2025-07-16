package org.mifos.workflow.core.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Represents metadata about a task within a workflow process.
 * Used to display or manage tasks (e.g., pending tasks for a user).
 */
@Data
@Builder
public class TaskInfo {
    private String taskId;
    private String name;
    private String processId;
    private String processDefinitionId;
    private String assignee;
    private LocalDateTime createTime;
    private LocalDateTime dueDate;
    private String description;
    private int priority;
}