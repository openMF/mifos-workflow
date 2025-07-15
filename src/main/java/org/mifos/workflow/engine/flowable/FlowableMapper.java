package org.mifos.workflow.engine.flowable;

import org.flowable.engine.repository.Deployment;
import org.flowable.task.api.Task;
import org.mifos.workflow.core.model.DeploymentInfo;
import org.mifos.workflow.core.model.HistoricProcessInstance;
import org.mifos.workflow.core.model.ProcessDefinition;
import org.mifos.workflow.core.model.ProcessInstance;
import org.mifos.workflow.core.model.TaskInfo;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Mapper class for converting Flowable engine objects to domain model objects.
 * This class provides mapping methods to convert between Flowable-specific objects
 * and the application's domain model, improving separation of concerns and testability.
 */
@Component
public class FlowableMapper {

    public ProcessDefinition mapToProcessDefinition(org.flowable.engine.repository.ProcessDefinition flowableDef) {
        return ProcessDefinition.builder()
                .id(flowableDef.getId())
                .key(flowableDef.getKey())
                .name(flowableDef.getName())
                .version(flowableDef.getVersion())
                .deploymentId(flowableDef.getDeploymentId())
                .build();
    }


    public ProcessInstance mapToProcessInstance(org.flowable.engine.runtime.ProcessInstance flowableInstance) {
        return ProcessInstance.builder()
                .id(flowableInstance.getId())
                .processDefinitionId(flowableInstance.getProcessDefinitionId())
                .businessKey(flowableInstance.getBusinessKey())
                .status(flowableInstance.isEnded() ? "completed" : "active")
                .startTime(LocalDateTime.ofInstant(flowableInstance.getStartTime().toInstant(), ZoneId.systemDefault()))
                .build();
    }


    public DeploymentInfo mapToDeploymentInfo(Deployment deployment) {
        return DeploymentInfo.builder()
                .id(deployment.getId())
                .name(deployment.getName())
                .deploymentTime(LocalDateTime.ofInstant(deployment.getDeploymentTime().toInstant(), ZoneId.systemDefault()))
                .build();
    }


    public TaskInfo mapToTaskInfo(Task task) {
        return TaskInfo.builder()
                .taskId(task.getId())
                .name(task.getName())
                .description(task.getDescription())
                .assignee(task.getAssignee())
                .processId(task.getProcessInstanceId())
                .processDefinitionId(task.getProcessDefinitionId())
                .createTime(LocalDateTime.ofInstant(task.getCreateTime().toInstant(), ZoneId.systemDefault()))
                .dueDate(task.getDueDate() != null ? 
                    LocalDateTime.ofInstant(task.getDueDate().toInstant(), ZoneId.systemDefault()) : null)
                .priority(task.getPriority())
                .build();
    }


    public HistoricProcessInstance mapToHistoricProcessInstance(org.flowable.engine.history.HistoricProcessInstance historicInstance) {
        return HistoricProcessInstance.builder()
                .id(historicInstance.getId())
                .processDefinitionId(historicInstance.getProcessDefinitionId())
                .businessKey(historicInstance.getBusinessKey())
                .startTime(LocalDateTime.ofInstant(historicInstance.getStartTime().toInstant(), ZoneId.systemDefault()))
                .endTime(historicInstance.getEndTime() != null ? 
                    LocalDateTime.ofInstant(historicInstance.getEndTime().toInstant(), ZoneId.systemDefault()) : null)
                .durationInMillis(historicInstance.getDurationInMillis())
                .outcome(historicInstance.getEndActivityId())
                .build();
    }
} 