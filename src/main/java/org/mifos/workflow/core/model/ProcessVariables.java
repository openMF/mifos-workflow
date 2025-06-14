package org.mifos.workflow.core.model;

import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents variables associated with a process or task in a workflow.
 * These variables store dynamic data used during workflow execution.
 */
@Data
@Builder
public class ProcessVariables {

    private Map<String, Object> variables;

    public ProcessVariables() {
        this.variables = new HashMap<>();
    }

    public ProcessVariables(Map<String, Object> variables) {
        this.variables = variables != null ? variables : new HashMap<>();
    }
}