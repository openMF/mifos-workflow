package org.mifos.workflow.core.engine;

import org.mifos.workflow.core.engine.enums.EngineType;
import org.mifos.workflow.core.model.*;

import java.io.InputStream;
import java.util.List;

/**
 * Generic interface for workflow engine operations.
 * This interface abstracts the workflow engine implementation details,
 * allowing for easy replacement of the underlying engine without affecting
 * Mifos X core logic.
 */
public interface WorkflowEngine {

    List<ProcessDefinition> getProcessDefinitions();

    DeploymentResult deployProcess(InputStream processDefinition, String filename);

    void deleteDeployment(String deploymentId);

    List<DeploymentInfo> getDeployments();

    // Process Instance Operations
    ProcessInstance startProcess(String processDefinitionKey, ProcessVariables variables);

    List<ProcessInstance> getProcessInstances();

    ProcessVariables getProcessVariables(String processInstanceId);

    // Task Operations
    void completeTask(String taskId, ProcessVariables variables);

    List<TaskInfo> getPendingTasks(String userId);

    List<TaskInfo> getPendingTasksForProcess(String processInstanceId);

    ProcessVariables getTaskVariables(String taskId);

    // History Operations
    List<HistoricProcessInstance> getHistoricProcesses();

    ProcessVariables getHistoricProcessVariables(String processInstanceId);

    HistoricProcessInstance getHistoricProcessInstance(String processInstanceId);

    ProcessHistory getProcessHistory(String processInstanceId);

    // Engine Operations
    EngineType getEngineType();

    boolean isEngineActive();

    boolean isProcessActive(String processInstanceId);

    // Replay Operation (for Task Execution Replay feature)
    ProcessInstance replayProcess(String processInstanceId, ProcessVariables variables);
}