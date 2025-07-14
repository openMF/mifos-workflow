package org.mifos.workflow.engine.flowable;

import org.flowable.engine.ProcessEngine;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.HistoryService;
import org.flowable.engine.repository.Deployment;
import org.flowable.task.api.Task;
import org.flowable.variable.api.history.HistoricVariableInstance;
import org.mifos.workflow.config.WorkflowConfig;
import org.mifos.workflow.core.engine.WorkflowEngine;
import org.mifos.workflow.core.engine.enums.EngineType;
import org.mifos.workflow.core.model.ActiveProcess;
import org.mifos.workflow.core.model.DeploymentInfo;
import org.mifos.workflow.core.model.DeploymentInfoEnhanced;
import org.mifos.workflow.core.model.DeploymentResource;
import org.mifos.workflow.core.model.DeploymentResult;
import org.mifos.workflow.core.model.HistoricProcessInstance;
import org.mifos.workflow.core.model.ProcessCompletionStatus;
import org.mifos.workflow.core.model.ProcessDefinition;
import org.mifos.workflow.core.model.ProcessDefinitionInfo;
import org.mifos.workflow.core.model.ProcessHistory;
import org.mifos.workflow.core.model.ProcessHistoryInfo;
import org.mifos.workflow.core.model.ProcessInstance;
import org.mifos.workflow.core.model.ProcessStatus;
import org.mifos.workflow.core.model.ProcessVariables;
import org.mifos.workflow.core.model.TaskInfo;
import org.mifos.workflow.util.WorkflowErrorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * FlowableWorkflowEngine is an implementation of the WorkflowEngine interface
 * that integrates with the Flowable workflow engine.
 * This class provides methods to manage process definitions, deployments,
 * process instances, tasks, and historical data.
 */
@Component
public class FlowableWorkflowEngine implements WorkflowEngine {

    private static final Logger logger = LoggerFactory.getLogger(FlowableWorkflowEngine.class);
    private final WorkflowConfig properties;
    private final FlowableMapper flowableMapper;
    private ProcessEngine processEngine;
    private RepositoryService repositoryService;
    private RuntimeService runtimeService;
    private TaskService taskService;
    private HistoryService historyService;

    @Autowired
    public FlowableWorkflowEngine(WorkflowConfig properties, org.flowable.engine.ProcessEngine processEngine, FlowableMapper flowableMapper) {
        this.properties = properties;
        this.flowableMapper = flowableMapper;
        this.processEngine = processEngine;
        this.repositoryService = processEngine.getRepositoryService();
        this.runtimeService = processEngine.getRuntimeService();
        this.taskService = processEngine.getTaskService();
        this.historyService = processEngine.getHistoryService();

        logger.info("FlowableWorkflowEngine initialized successfully with Spring-managed ProcessEngine");
    }

    @Override
    public List<ProcessDefinition> getProcessDefinitions() {
        return WorkflowErrorHandler.executeWithExceptionHandling("retrieving process definitions", "all", () -> {
            List<org.flowable.engine.repository.ProcessDefinition> flowableDefinitions = repositoryService.createProcessDefinitionQuery().active().list();

            return flowableDefinitions.stream().map(flowableMapper::mapToProcessDefinition).collect(Collectors.toList());
        });
    }

    @Override
    public DeploymentResult deployProcess(InputStream processDefinition, String filename) {
        return WorkflowErrorHandler.executeWithExceptionHandling("process deployment", filename, () -> {
            Deployment deployment = repositoryService.createDeployment().addInputStream(filename, processDefinition).name(filename).deploy();

            logger.info("Successfully deployed process: {} with deployment ID: {}", filename, deployment.getId());

            return DeploymentResult.builder().deploymentId(deployment.getId()).name(deployment.getName()).deploymentTime(LocalDateTime.ofInstant(deployment.getDeploymentTime().toInstant(), ZoneId.systemDefault())).success(true).errors(Collections.emptyList()).build();
        });
    }

    @Override
    public void deleteDeployment(String deploymentId) {
        WorkflowErrorHandler.executeWithExceptionHandling("deployment deletion", deploymentId, () -> {
            repositoryService.deleteDeployment(deploymentId, true);
            logger.info("Successfully deleted deployment: {}", deploymentId);
        });
    }

    @Override
    public List<DeploymentInfo> getDeployments() {
        return WorkflowErrorHandler.executeWithExceptionHandling("retrieving deployments", "all", () -> {
            List<Deployment> deployments = repositoryService.createDeploymentQuery().orderByDeploymentTime().desc().list();

            return deployments.stream().map(flowableMapper::mapToDeploymentInfo).collect(Collectors.toList());
        });
    }

    @Override
    public ProcessInstance startProcess(String processDefinitionKey, ProcessVariables variables) {
        return WorkflowErrorHandler.executeWithExceptionHandling("process start", processDefinitionKey, () -> {
            Map<String, Object> flowableVariables = variables != null ? variables.getVariables() : new HashMap<>();

            org.flowable.engine.runtime.ProcessInstance flowableInstance = runtimeService.startProcessInstanceByKey(processDefinitionKey, flowableVariables);

            logger.info("Started process instance: {} for definition: {}", flowableInstance.getId(), processDefinitionKey);

            return flowableMapper.mapToProcessInstance(flowableInstance);
        });
    }

    @Override
    public List<ProcessInstance> getProcessInstances() {
        return WorkflowErrorHandler.executeWithExceptionHandling("retrieving process instances", "all", () -> {
            List<org.flowable.engine.runtime.ProcessInstance> flowableInstances = runtimeService.createProcessInstanceQuery().active().list();

            return flowableInstances.stream().map(flowableMapper::mapToProcessInstance).collect(Collectors.toList());
        });
    }

    @Override
    public ProcessVariables getProcessVariables(String processInstanceId) {
        return WorkflowErrorHandler.executeWithExceptionHandling("retrieving process variables", processInstanceId, () -> {
            Map<String, Object> variables = runtimeService.getVariables(processInstanceId);
            return ProcessVariables.builder().variables(variables).build();
        });
    }

    @Override
    public void completeTask(String taskId, ProcessVariables variables) {
        WorkflowErrorHandler.executeWithExceptionHandling("task completion", taskId, () -> {
            Map<String, Object> flowableVariables = variables != null ? variables.getVariables() : new HashMap<>();
            taskService.complete(taskId, flowableVariables);
            logger.info("Completed task: {}", taskId);
        });
    }

    @Override
    public List<TaskInfo> getPendingTasks(String userId) {
        return WorkflowErrorHandler.executeWithExceptionHandling("retrieving pending tasks for user", userId, () -> {
            List<Task> tasks = taskService.createTaskQuery().taskAssignee(userId).list();

            return tasks.stream().map(flowableMapper::mapToTaskInfo).collect(Collectors.toList());
        });
    }

    @Override
    public List<TaskInfo> getPendingTasksForProcess(String processInstanceId) {
        return WorkflowErrorHandler.executeWithExceptionHandling("retrieving pending tasks for process", processInstanceId, () -> {
            List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstanceId).list();

            return tasks.stream().map(flowableMapper::mapToTaskInfo).collect(Collectors.toList());
        });
    }

    @Override
    public ProcessVariables getTaskVariables(String taskId) {
        return WorkflowErrorHandler.executeWithExceptionHandling("retrieving task variables", taskId, () -> {
            Map<String, Object> variables = taskService.getVariables(taskId);
            return ProcessVariables.builder().variables(variables).build();
        });
    }

    @Override
    public List<HistoricProcessInstance> getHistoricProcesses() {
        return WorkflowErrorHandler.executeWithExceptionHandling("retrieving historic processes", "all", () -> {
            List<org.flowable.engine.history.HistoricProcessInstance> historicInstances = historyService.createHistoricProcessInstanceQuery().finished().orderByProcessInstanceEndTime().desc().list();

            return historicInstances.stream().map(flowableMapper::mapToHistoricProcessInstance).collect(Collectors.toList());
        });
    }

    @Override
    public ProcessVariables getHistoricProcessVariables(String processInstanceId) {
        return WorkflowErrorHandler.executeWithExceptionHandling("retrieving historic process variables", processInstanceId, () -> {
            org.flowable.engine.history.HistoricProcessInstance historicInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();

            if (historicInstance == null) {
                logger.warn("Historic process instance not found: {}", processInstanceId);
                throw new RuntimeException("Historic process instance not found: " + processInstanceId);
            }

            List<HistoricVariableInstance> variables = historyService.createHistoricVariableInstanceQuery().processInstanceId(processInstanceId).list();

            Map<String, Object> variableMap = new HashMap<>();
            for (HistoricVariableInstance variable : variables) {
                if (variable != null && variable.getVariableName() != null) {
                    String variableName = variable.getVariableName();
                    Object variableValue = variable.getValue() != null ? variable.getValue() : "null";
                    variableMap.put(variableName, variableValue);
                }
            }

            return ProcessVariables.builder().variables(variableMap).build();
        });
    }

    @Override
    public HistoricProcessInstance getHistoricProcessInstance(String processInstanceId) {
        return WorkflowErrorHandler.executeWithExceptionHandling("retrieving historic process instance", processInstanceId, () -> {
            org.flowable.engine.history.HistoricProcessInstance historicInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();

            return historicInstance != null ? flowableMapper.mapToHistoricProcessInstance(historicInstance) : null;
        });
    }

    @Override
    public ProcessHistory getProcessHistory(String processInstanceId) {
        return WorkflowErrorHandler.executeWithExceptionHandling("retrieving process history", processInstanceId, () -> {
            org.flowable.engine.history.HistoricProcessInstance historicInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();

            if (historicInstance == null) {
                return null;
            }

            return ProcessHistory.builder().historyId(historicInstance.getId()).processId(processInstanceId).processDefinitionId(historicInstance.getProcessDefinitionId()).startTime(LocalDateTime.ofInstant(historicInstance.getStartTime().toInstant(), ZoneId.systemDefault())).endTime(historicInstance.getEndTime() != null ? LocalDateTime.ofInstant(historicInstance.getEndTime().toInstant(), ZoneId.systemDefault()) : null).durationInMillis(historicInstance.getDurationInMillis()).build();
        });
    }

    @Override
    public EngineType getEngineType() {
        return EngineType.FLOWABLE;
    }


    @Override
    public boolean isEngineActive() {
        return processEngine != null;
    }

    @Override
    public boolean isProcessActive(String processInstanceId) {
        return WorkflowErrorHandler.executeWithExceptionHandling("checking process active status", processInstanceId, () -> {
            org.flowable.engine.runtime.ProcessInstance instance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
            return instance != null;
        });
    }

    @Override
    public ProcessInstance replayProcess(String processInstanceId, ProcessVariables variables) {
        return WorkflowErrorHandler.executeWithExceptionHandling("process replay", processInstanceId, () -> {
            org.flowable.engine.history.HistoricProcessInstance historicInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();

            if (historicInstance == null) {
                throw new RuntimeException("Historic process instance not found: " + processInstanceId);
            }

            Map<String, Object> flowableVariables = variables != null ? variables.getVariables() : new HashMap<>();
            org.flowable.engine.runtime.ProcessInstance newInstance = runtimeService.startProcessInstanceByKey(historicInstance.getProcessDefinitionKey(), flowableVariables);

            logger.info("Replayed process instance: {} as new instance: {}", processInstanceId, newInstance.getId());

            return flowableMapper.mapToProcessInstance(newInstance);
        });
    }

    @Override
    public void terminateProcess(String processInstanceId, String reason) {
        WorkflowErrorHandler.executeWithExceptionHandling("process termination", processInstanceId, () -> {
            runtimeService.deleteProcessInstance(processInstanceId, reason);
            logger.info("Terminated process instance: {} with reason: {}", processInstanceId, reason);
        });
    }

    @Override
    public ProcessStatus getProcessStatus(String processInstanceId) {
        return WorkflowErrorHandler.executeWithExceptionHandling("getting process status", processInstanceId, () -> {
            org.flowable.engine.runtime.ProcessInstance flowableInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();

            if (flowableInstance == null) {
                org.flowable.engine.history.HistoricProcessInstance historicInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();

                if (historicInstance == null) {
                    throw new RuntimeException("Process instance not found: " + processInstanceId);
                }

                org.flowable.engine.repository.ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(historicInstance.getProcessDefinitionId()).singleResult();

                return ProcessStatus.builder().processInstanceId(processInstanceId).processDefinitionKey(processDefinition != null ? processDefinition.getKey() : null).processDefinitionName(processDefinition != null ? processDefinition.getName() : null).status("COMPLETED").currentActivityName(null).currentActivityId(null).startTime(LocalDateTime.ofInstant(historicInstance.getStartTime().toInstant(), ZoneId.systemDefault())).endTime(historicInstance.getEndTime() != null ? LocalDateTime.ofInstant(historicInstance.getEndTime().toInstant(), ZoneId.systemDefault()) : null).businessKey(historicInstance.getBusinessKey()).duration(historicInstance.getDurationInMillis()).startedBy(historicInstance.getStartUserId()).variables(new HashMap<>()).assignee(null).suspended(false).ended(true).build();
            }

            org.flowable.engine.repository.ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(flowableInstance.getProcessDefinitionId()).singleResult();

            String currentActivityId = null;
            String currentActivityName = null;
            List<org.flowable.engine.runtime.Execution> executions = runtimeService.createExecutionQuery().processInstanceId(processInstanceId).list();

            if (!executions.isEmpty()) {
                for (org.flowable.engine.runtime.Execution execution : executions) {
                    if (execution.getActivityId() != null) {
                        currentActivityId = execution.getActivityId();
                        try {
                            org.flowable.bpmn.model.BpmnModel bpmnModel = repositoryService.getBpmnModel(flowableInstance.getProcessDefinitionId());
                            org.flowable.bpmn.model.FlowElement flowElement = bpmnModel.getFlowElement(currentActivityId);
                            if (flowElement != null) {
                                currentActivityName = flowElement.getName();
                            }
                        } catch (Exception e) {
                            logger.debug("Could not get activity name for activity ID: {}", currentActivityId);
                        }
                        break;
                    }
                }
            }

            String assignee = null;
            List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstanceId).list();
            if (!tasks.isEmpty()) {
                assignee = tasks.get(0).getAssignee();
            }

            Map<String, Object> variables = runtimeService.getVariables(processInstanceId);

            return ProcessStatus.builder().processInstanceId(processInstanceId).processDefinitionKey(processDefinition != null ? processDefinition.getKey() : null).processDefinitionName(processDefinition != null ? processDefinition.getName() : null).status("ACTIVE").currentActivityName(currentActivityName).currentActivityId(currentActivityId).startTime(LocalDateTime.ofInstant(flowableInstance.getStartTime().toInstant(), ZoneId.systemDefault())).endTime(null).businessKey(flowableInstance.getBusinessKey()).duration(System.currentTimeMillis() - flowableInstance.getStartTime().getTime()).startedBy(flowableInstance.getStartUserId()).variables(variables).assignee(assignee).suspended(flowableInstance.isSuspended()).ended(flowableInstance.isEnded()).build();
        });
    }

    @Override
    public ProcessCompletionStatus getProcessCompletionStatus(String processInstanceId) {
        return WorkflowErrorHandler.executeWithExceptionHandling("getting process completion status", processInstanceId, () -> {
            org.flowable.engine.history.HistoricProcessInstance historicInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();

            if (historicInstance == null) {
                throw new RuntimeException("Historic process instance not found: " + processInstanceId);
            }

            if (historicInstance.getEndTime() == null) {
                org.flowable.engine.repository.ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(historicInstance.getProcessDefinitionId()).singleResult();

                Map<String, Object> variables = new HashMap<>();
                List<HistoricVariableInstance> variableInstances = historyService.createHistoricVariableInstanceQuery().processInstanceId(processInstanceId).list();

                for (HistoricVariableInstance variable : variableInstances) {
                    if (variable != null && variable.getVariableName() != null) {
                        variables.put(variable.getVariableName(), variable.getValue());
                    }
                }

                return ProcessCompletionStatus.builder().processInstanceId(processInstanceId).processDefinitionKey(processDefinition != null ? processDefinition.getKey() : null).processDefinitionName(processDefinition != null ? processDefinition.getName() : null).outcome("IN_PROGRESS").completionReason(null).startTime(LocalDateTime.ofInstant(historicInstance.getStartTime().toInstant(), ZoneId.systemDefault())).endTime(null).businessKey(historicInstance.getBusinessKey()).duration(System.currentTimeMillis() - historicInstance.getStartTime().getTime()).startedBy(historicInstance.getStartUserId()).variables(variables).completedBy(null).errorMessage(null).stackTrace(null).successful(null).build();
            }

            org.flowable.engine.repository.ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(historicInstance.getProcessDefinitionId()).singleResult();

            Map<String, Object> variables = new HashMap<>();
            List<HistoricVariableInstance> variableInstances = historyService.createHistoricVariableInstanceQuery().processInstanceId(processInstanceId).list();

            for (HistoricVariableInstance variable : variableInstances) {
                if (variable != null && variable.getVariableName() != null) {
                    variables.put(variable.getVariableName(), variable.getValue());
                }
            }

            String completionReason = historicInstance.getDeleteReason();
            String outcome = completionReason != null ? "TERMINATED" : "SUCCESS";
            Boolean successful = completionReason == null;

            return ProcessCompletionStatus.builder().processInstanceId(processInstanceId).processDefinitionKey(processDefinition != null ? processDefinition.getKey() : null).processDefinitionName(processDefinition != null ? processDefinition.getName() : null).outcome(outcome).completionReason(completionReason).startTime(LocalDateTime.ofInstant(historicInstance.getStartTime().toInstant(), ZoneId.systemDefault())).endTime(LocalDateTime.ofInstant(historicInstance.getEndTime().toInstant(), ZoneId.systemDefault())).businessKey(historicInstance.getBusinessKey()).duration(historicInstance.getDurationInMillis()).startedBy(historicInstance.getStartUserId()).variables(variables).completedBy(null).errorMessage(null).stackTrace(null).successful(successful).build();
        });
    }

    @Override
    public List<ActiveProcess> getActiveProcesses() {
        return WorkflowErrorHandler.executeWithExceptionHandling("getting active processes", "all", () -> {
            List<org.flowable.engine.runtime.ProcessInstance> flowableInstances = runtimeService.createProcessInstanceQuery().active().list();

            return flowableInstances.stream().map(flowableInstance -> {
                org.flowable.engine.repository.ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(flowableInstance.getProcessDefinitionId()).singleResult();

                String currentActivityId = null;
                String currentActivityName = null;
                List<org.flowable.engine.runtime.Execution> executions = runtimeService.createExecutionQuery().processInstanceId(flowableInstance.getId()).list();

                if (!executions.isEmpty()) {
                    for (org.flowable.engine.runtime.Execution execution : executions) {
                        if (execution.getActivityId() != null) {
                            currentActivityId = execution.getActivityId();
                            try {
                                org.flowable.bpmn.model.BpmnModel bpmnModel = repositoryService.getBpmnModel(flowableInstance.getProcessDefinitionId());
                                org.flowable.bpmn.model.FlowElement flowElement = bpmnModel.getFlowElement(currentActivityId);
                                if (flowElement != null) {
                                    currentActivityName = flowElement.getName();
                                }
                            } catch (Exception e) {
                                logger.debug("Could not get activity name for activity ID: {}", currentActivityId);
                            }
                            break;
                        }
                    }
                }

                String assignee = null;
                List<Task> tasks = taskService.createTaskQuery().processInstanceId(flowableInstance.getId()).list();
                if (!tasks.isEmpty()) {
                    assignee = tasks.get(0).getAssignee();
                }

                Map<String, Object> variables = runtimeService.getVariables(flowableInstance.getId());

                return ActiveProcess.builder().processInstanceId(flowableInstance.getId()).processDefinitionKey(processDefinition != null ? processDefinition.getKey() : null).processDefinitionName(processDefinition != null ? processDefinition.getName() : null).status("ACTIVE").currentActivityName(currentActivityName).currentActivityId(currentActivityId).startTime(LocalDateTime.ofInstant(flowableInstance.getStartTime().toInstant(), ZoneId.systemDefault())).businessKey(flowableInstance.getBusinessKey()).duration(System.currentTimeMillis() - flowableInstance.getStartTime().getTime()).startedBy(flowableInstance.getStartUserId()).variables(variables).assignee(assignee).build();
            }).collect(Collectors.toList());
        });
    }

    @Override
    public List<ProcessDefinitionInfo> getProcessDefinitionsInfo() {
        return WorkflowErrorHandler.executeWithExceptionHandling("getting process definitions info", "all", () -> {
            List<org.flowable.engine.repository.ProcessDefinition> flowableDefinitions = repositoryService.createProcessDefinitionQuery().active().orderByProcessDefinitionKey().asc().list();

            return flowableDefinitions.stream().map(flowableDefinition -> {
                org.flowable.engine.repository.Deployment deployment = repositoryService.createDeploymentQuery().deploymentId(flowableDefinition.getDeploymentId()).singleResult();

                long activeInstances = runtimeService.createProcessInstanceQuery().processDefinitionId(flowableDefinition.getId()).active().count();

                long totalInstances = historyService.createHistoricProcessInstanceQuery().processDefinitionId(flowableDefinition.getId()).count();

                return ProcessDefinitionInfo.builder().processDefinitionId(flowableDefinition.getId()).processDefinitionKey(flowableDefinition.getKey()).processDefinitionName(flowableDefinition.getName()).version(flowableDefinition.getVersion()).deploymentId(flowableDefinition.getDeploymentId()).deploymentName(deployment != null ? deployment.getName() : null).deploymentTime(deployment != null ? LocalDateTime.ofInstant(deployment.getDeploymentTime().toInstant(), ZoneId.systemDefault()) : null).resourceName(flowableDefinition.getResourceName()).diagramResourceName(flowableDefinition.getDiagramResourceName()).description(flowableDefinition.getDescription()).suspended(flowableDefinition.isSuspended()).category(flowableDefinition.getCategory()).properties(new HashMap<>()).engineType("FLOWABLE").activeInstances((int) activeInstances).totalInstances((int) totalInstances).build();
            }).collect(Collectors.toList());
        });
    }

    @Override
    public List<ProcessHistoryInfo> getProcessHistoryInfo() {
        return WorkflowErrorHandler.executeWithExceptionHandling("getting process history info", "all", () -> {
            List<org.flowable.engine.history.HistoricProcessInstance> historicInstances = historyService.createHistoricProcessInstanceQuery().finished().orderByProcessInstanceEndTime().desc().list();

            return historicInstances.stream().map(historicInstance -> {
                org.flowable.engine.repository.ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(historicInstance.getProcessDefinitionId()).singleResult();

                Map<String, Object> variables = new HashMap<>();
                List<HistoricVariableInstance> variableInstances = historyService.createHistoricVariableInstanceQuery().processInstanceId(historicInstance.getId()).list();

                for (HistoricVariableInstance variable : variableInstances) {
                    if (variable != null && variable.getVariableName() != null) {
                        variables.put(variable.getVariableName(), variable.getValue());
                    }
                }

                String completionReason = historicInstance.getDeleteReason();
                String status = completionReason != null ? "TERMINATED" : "COMPLETED";
                Boolean successful = completionReason == null;

                return ProcessHistoryInfo.builder().historicProcessInstanceId(historicInstance.getId()).processInstanceId(historicInstance.getId()).processDefinitionKey(processDefinition != null ? processDefinition.getKey() : null).processDefinitionName(processDefinition != null ? processDefinition.getName() : null).processDefinitionVersion(processDefinition != null ? processDefinition.getVersion() : null).startTime(LocalDateTime.ofInstant(historicInstance.getStartTime().toInstant(), ZoneId.systemDefault())).endTime(LocalDateTime.ofInstant(historicInstance.getEndTime().toInstant(), ZoneId.systemDefault())).duration(historicInstance.getDurationInMillis()).startedBy(historicInstance.getStartUserId()).completedBy(null).status(status).completionReason(completionReason).businessKey(historicInstance.getBusinessKey()).variables(variables).deploymentId(historicInstance.getDeploymentId()).successful(successful).errorMessage(null).stackTrace(null).category(processDefinition != null ? processDefinition.getCategory() : null).description(processDefinition != null ? processDefinition.getDescription() : null).build();
            }).collect(Collectors.toList());
        });
    }

    @Override
    public DeploymentInfoEnhanced getDeploymentInfo(String deploymentId) {
        return WorkflowErrorHandler.executeWithExceptionHandling("getting deployment info", deploymentId, () -> {
            Deployment deployment = repositoryService.createDeploymentQuery().deploymentId(deploymentId).singleResult();

            if (deployment == null) {
                throw new RuntimeException("Deployment not found: " + deploymentId);
            }

            return DeploymentInfoEnhanced.builder().id(deployment.getId()).name(deployment.getName()).deploymentTime(LocalDateTime.ofInstant(deployment.getDeploymentTime().toInstant(), ZoneId.systemDefault())).category(deployment.getCategory()).tenantId(deployment.getTenantId()).properties(new HashMap<>()).metadata(new HashMap<>()).deploymentData(new HashMap<>()).configuration(new HashMap<>()).build();
        });
    }

    @Override
    public List<DeploymentResource> getDeploymentResources(String deploymentId) {
        return WorkflowErrorHandler.executeWithExceptionHandling("getting deployment resources", deploymentId, () -> {
            List<String> resourceNames = repositoryService.getDeploymentResourceNames(deploymentId);

            return resourceNames.stream().map(resourceName -> {
                InputStream resourceStream = repositoryService.getResourceAsStream(deploymentId, resourceName);
                byte[] resourceBytes = new byte[0];
                try {
                    resourceBytes = resourceStream.readAllBytes();
                } catch (Exception e) {
                    logger.warn("Could not read resource: {}", resourceName, e);
                }

                return DeploymentResource.builder().deploymentId(deploymentId).name(resourceName).resourceType("application/octet-stream").resourceSize((long) resourceBytes.length).checksum(String.valueOf(Arrays.hashCode(resourceBytes))).properties(new HashMap<>()).metadata(new HashMap<>()).build();
            }).collect(Collectors.toList());
        });
    }

    @Override
    public byte[] getDeploymentResource(String deploymentId, String resourceName) {
        return WorkflowErrorHandler.executeWithExceptionHandling("getting deployment resource", deploymentId + "/" + resourceName, () -> {
            java.io.InputStream resourceStream = repositoryService.getResourceAsStream(deploymentId, resourceName);

            if (resourceStream == null) {
                throw new IllegalArgumentException("Resource not found: " + resourceName + " in deployment: " + deploymentId);
            }

            try {
                return resourceStream.readAllBytes();
            } catch (java.io.IOException e) {
                throw new RuntimeException("Failed to read deployment resource", e);
            }
        });
    }
}