package org.mifos.workflow.engine.flowable;

import org.mifos.workflow.config.WorkflowProperties;
import org.mifos.workflow.core.engine.WorkflowEngine;
import org.mifos.workflow.core.engine.enums.EngineType;
import org.mifos.workflow.core.model.DeploymentInfo;
import org.mifos.workflow.core.model.DeploymentResult;
import org.mifos.workflow.core.model.HistoricProcessInstance;
import org.mifos.workflow.core.model.ProcessDefinition;
import org.mifos.workflow.core.model.ProcessHistory;
import org.mifos.workflow.core.model.ProcessInstance;
import org.mifos.workflow.core.model.ProcessVariables;
import org.mifos.workflow.core.model.TaskInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class FlowableWorkflowEngine implements WorkflowEngine {

    private static final Logger logger = LoggerFactory.getLogger(FlowableWorkflowEngine.class);
    private final WorkflowProperties properties;

    public FlowableWorkflowEngine(WorkflowProperties properties) {
        this.properties = properties;
        logger.info("FlowableWorkflowEngine initialized with properties: {}", properties.getEngine().getType());
    }

    @Override
    public List<ProcessDefinition> getProcessDefinitions() {
        logger.warn("Flowable getProcessDefinitions not implemented yet");
        return Collections.emptyList();
    }

    @Override
    public DeploymentResult deployProcess(InputStream processDefinition, String filename) {
        logger.warn("Flowable deployProcess not implemented yet");
        return DeploymentResult.builder()
                .deploymentId("stub-deployment-id")
                .name(filename)
                .deploymentTime(LocalDateTime.now())
                .success(false)
                .errors(Collections.singletonList("Not implemented yet"))
                .build();
    }

    @Override
    public void deleteDeployment(String deploymentId) {
        logger.warn("Flowable deleteDeployment not implemented yet");
    }

    @Override
    public List<DeploymentInfo> getDeployments() {
        logger.warn("Flowable getDeployments not implemented yet");
        return Collections.emptyList();
    }

    @Override
    public ProcessInstance startProcess(String processDefinitionKey, ProcessVariables variables) {
        logger.warn("Flowable startProcess not implemented yet");
        return ProcessInstance.builder()
                .id("stub-process-id")
                .processDefinitionId("stub-definition-id")
                .businessKey("stub-business-key")
                .status("active")
                .startTime(LocalDateTime.now())
                .build();
    }

    @Override
    public List<ProcessInstance> getProcessInstances() {
        logger.warn("Flowable getProcessInstances not implemented yet");
        return Collections.emptyList();
    }

    @Override
    public ProcessVariables getProcessVariables(String processInstanceId) {
        logger.warn("Flowable getProcessVariables not implemented yet");
        return ProcessVariables.builder()
                .variables(new HashMap<>())
                .build();
    }

    @Override
    public void completeTask(String taskId, ProcessVariables variables) {
        logger.warn("Flowable completeTask not implemented yet");
    }

    @Override
    public List<TaskInfo> getPendingTasks(String userId) {
        logger.warn("Flowable getPendingTasks not implemented yet");
        return Collections.emptyList();
    }

    @Override
    public List<TaskInfo> getPendingTasksForProcess(String processInstanceId) {
        logger.warn("Flowable getPendingTasksForProcess not implemented yet");
        return Collections.emptyList();
    }

    @Override
    public ProcessVariables getTaskVariables(String taskId) {
        logger.warn("Flowable getTaskVariables not implemented yet");
        return ProcessVariables.builder()
                .variables(new HashMap<>())
                .build();
    }

    @Override
    public List<HistoricProcessInstance> getHistoricProcesses() {
        logger.warn("Flowable getHistoricProcesses not implemented yet");
        return Collections.emptyList();
    }

    @Override
    public ProcessVariables getHistoricProcessVariables(String processInstanceId) {
        logger.warn("Flowable getHistoricProcessVariables not implemented yet");
        return ProcessVariables.builder()
                .variables(new HashMap<>())
                .build();
    }

    @Override
    public HistoricProcessInstance getHistoricProcessInstance(String processInstanceId) {
        logger.warn("Flowable getHistoricProcessInstance not implemented yet");
        return HistoricProcessInstance.builder()
                .id(processInstanceId)
                .processDefinitionId("stub-definition-id")
                .businessKey("stub-business-key")
                .startTime(LocalDateTime.now().minusHours(2))
                .endTime(LocalDateTime.now())
                .durationInMillis(7200000L)
                .outcome("completed")
                .build();
    }

    @Override
    public ProcessHistory getProcessHistory(String processInstanceId) {
        logger.warn("Flowable getProcessHistory not implemented yet");
        return ProcessHistory.builder()
                .historyId("stub-history-id")
                .processId(processInstanceId)
                .processDefinitionId("stub-definition-id")
                .startTime(LocalDateTime.now().minusHours(2))
                .endTime(LocalDateTime.now())
                .durationInMillis(7200000L)
                .build();
    }

    @Override
    public EngineType getEngineType() {
        return EngineType.FLOWABLE;
    }

    @Override
    public boolean isEngineActive() {
        logger.warn("Flowable isEngineActive not implemented yet");
        return false;
    }

    @Override
    public boolean isProcessActive(String processInstanceId) {
        logger.warn("Flowable isProcessActive not implemented yet");
        return false;
    }

    @Override
    public ProcessInstance replayProcess(String processInstanceId, ProcessVariables variables) {
        logger.warn("Flowable replayProcess not implemented yet");
        return ProcessInstance.builder()
                .id("stub-replayed-process-id")
                .processDefinitionId("stub-definition-id")
                .businessKey("stub-business-key")
                .status("active")
                .startTime(LocalDateTime.now())
                .build();
    }
}