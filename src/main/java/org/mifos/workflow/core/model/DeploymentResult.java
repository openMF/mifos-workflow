package org.mifos.workflow.core.model;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents the result of a deployment operation for a workflow definition.
 * Used to provide feedback on deployment success or failure.
 */
@Data
@Builder
@AllArgsConstructor
public class DeploymentResult {
    private String deploymentId;
    private String name;
    private LocalDateTime deploymentTime;
    private boolean success;
    private List<String> errors;

    public DeploymentResult() {
        this.success = false;
        this.errors = List.of();
    }
}