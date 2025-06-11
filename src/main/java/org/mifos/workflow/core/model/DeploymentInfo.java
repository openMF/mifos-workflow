package org.mifos.workflow.core.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Represents metadata about a deployed workflow definition.
 * Used to track and manage deployed workflows in the engine.
 */
@Data
@Builder
public class DeploymentInfo {
    private String id;
    private String name;
    private LocalDateTime deploymentTime;
}