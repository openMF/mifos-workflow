package org.mifos.workflow.core.model;

import lombok.Builder;
import lombok.Data;

/**
 * Represents a workflow process definition (a BPMN template).
 * Used to define workflows that can be instantiated as processes.
 */
@Data
@Builder
public class ProcessDefinition {
    private String id;
    private String key;
    private String name;
    private int version;
    private String deploymentId;
    private String description;
}