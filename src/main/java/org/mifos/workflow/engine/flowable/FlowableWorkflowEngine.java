package org.mifos.workflow.engine.flowable;

import org.flowable.engine.ProcessEngine;
import org.flowable.engine.ProcessEngineConfiguration;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
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
    private ProcessEngine processEngine;
    private RepositoryService repositoryService;
    private RuntimeService runtimeService;
    private TaskService taskService;
    private HistoryService historyService;

    @Autowired
    public FlowableWorkflowEngine(WorkflowConfig properties, DataSource dataSource) {
        this.properties = properties;
        initializeEngine(dataSource);
        logger.info("FlowableWorkflowEngine initialized successfully");
    }

    private void initializeEngine(DataSource dataSource) {
        try {
            ProcessEngineConfiguration config = ProcessEngineConfiguration
                    .createStandaloneProcessEngineConfiguration()
                    .setDataSource(dataSource)
                    .setDatabaseSchemaUpdate(properties.getEngine().getFlowable().isDatabaseSchemaUpdate() ?
                            ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE :
                            ProcessEngineConfiguration.DB_SCHEMA_UPDATE_FALSE)
                    .setAsyncExecutorActivate(properties.getEngine().getFlowable().isAsyncExecutorEnabled());

            this.processEngine = config.buildProcessEngine();
            this.repositoryService = processEngine.getRepositoryService();
            this.runtimeService = processEngine.getRuntimeService();
            this.taskService = processEngine.getTaskService();
            this.historyService = processEngine.getHistoryService();

            logger.info("Flowable ProcessEngine initialized with configuration: {}", config);
        } catch (Exception e) {
            logger.error("Failed to initialize Flowable ProcessEngine", e);
            throw new RuntimeException("Failed to initialize Flowable ProcessEngine", e);
        }
    }

    @Override
    public List<ProcessDefinition> getProcessDefinitions() {
        try {
            List<org.flowable.engine.repository.ProcessDefinition> flowableDefinitions = repositoryService.createProcessDefinitionQuery()
                    .active()
                    .list();

            return flowableDefinitions.stream()
                    .map(this::mapToProcessDefinition)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error retrieving process definitions", e);
            return Collections.emptyList();
        }
    }

    @Override
    public DeploymentResult deployProcess(InputStream processDefinition, String filename) {
        try {
            Deployment deployment = repositoryService.createDeployment()
                    .addInputStream(filename, processDefinition)
                    .name(filename)
                    .deploy();

            logger.info("Successfully deployed process: {} with deployment ID: {}", filename, deployment.getId());

            return DeploymentResult.builder()
                    .deploymentId(deployment.getId())
                    .name(deployment.getName())
                    .deploymentTime(LocalDateTime.ofInstant(deployment.getDeploymentTime().toInstant(), ZoneId.systemDefault()))
                    .success(true)
                    .errors(Collections.emptyList())
                    .build();
        } catch (Exception e) {
            logger.error("Failed to deploy process: {}", filename, e);
            return DeploymentResult.builder()
                    .deploymentId(null)
                    .name(filename)
                    .deploymentTime(LocalDateTime.now())
                    .success(false)
                    .errors(Collections.singletonList(e.getMessage()))
                    .build();
        }
    }

    @Override
    public void deleteDeployment(String deploymentId) {
        try {
            repositoryService.deleteDeployment(deploymentId, true);
            logger.info("Successfully deleted deployment: {}", deploymentId);
        } catch (Exception e) {
            logger.error("Failed to delete deployment: {}", deploymentId, e);
            throw new RuntimeException("Failed to delete deployment: " + deploymentId, e);
        }
    }

    @Override
    public List<DeploymentInfo> getDeployments() {
        try {
            List<Deployment> deployments = repositoryService.createDeploymentQuery()
                    .orderByDeploymentTime()
                    .desc()
                    .list();

            return deployments.stream()
                    .map(this::mapToDeploymentInfo)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error retrieving deployments", e);
            return Collections.emptyList();
        }
    }

    @Override
    public ProcessInstance startProcess(String processDefinitionKey, ProcessVariables variables) {
        try {
            Map<String, Object> flowableVariables = variables != null ? variables.getVariables() : new HashMap<>();

            org.flowable.engine.runtime.ProcessInstance flowableInstance = runtimeService.startProcessInstanceByKey(
                    processDefinitionKey,
                    flowableVariables
            );

            logger.info("Started process instance: {} for definition: {}",
                    flowableInstance.getId(), processDefinitionKey);

            return mapToProcessInstance(flowableInstance);
        } catch (Exception e) {
            logger.error("Failed to start process: {}", processDefinitionKey, e);
            throw new RuntimeException("Failed to start process: " + processDefinitionKey, e);
        }
    }

    @Override
    public List<ProcessInstance> getProcessInstances() {
        try {
            List<org.flowable.engine.runtime.ProcessInstance> flowableInstances = runtimeService.createProcessInstanceQuery()
                    .active()
                    .list();

            return flowableInstances.stream()
                    .map(this::mapToProcessInstance)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error retrieving process instances", e);
            return Collections.emptyList();
        }
    }

    @Override
    public ProcessVariables getProcessVariables(String processInstanceId) {
        try {
            Map<String, Object> variables = runtimeService.getVariables(processInstanceId);
            return ProcessVariables.builder()
                    .variables(variables)
                    .build();
        } catch (Exception e) {
            logger.error("Error retrieving process variables for instance: {}", processInstanceId, e);
            return ProcessVariables.builder()
                    .variables(new HashMap<>())
                    .build();
        }
    }

    @Override
    public void completeTask(String taskId, ProcessVariables variables) {
        try {
            Map<String, Object> flowableVariables = variables != null ? variables.getVariables() : new HashMap<>();
            taskService.complete(taskId, flowableVariables);
            logger.info("Completed task: {}", taskId);
        } catch (Exception e) {
            logger.error("Failed to complete task: {}", taskId, e);
            throw new RuntimeException("Failed to complete task: " + taskId, e);
        }
    }

    @Override
    public List<TaskInfo> getPendingTasks(String userId) {
        try {
            List<Task> tasks = taskService.createTaskQuery()
                    .taskAssignee(userId)
                    .list();

            return tasks.stream()
                    .map(this::mapToTaskInfo)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error retrieving pending tasks for user: {}", userId, e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<TaskInfo> getPendingTasksForProcess(String processInstanceId) {
        try {
            List<Task> tasks = taskService.createTaskQuery()
                    .processInstanceId(processInstanceId)
                    .list();

            return tasks.stream()
                    .map(this::mapToTaskInfo)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error retrieving pending tasks for process: {}", processInstanceId, e);
            return Collections.emptyList();
        }
    }

    @Override
    public ProcessVariables getTaskVariables(String taskId) {
        try {
            Map<String, Object> variables = taskService.getVariables(taskId);
            return ProcessVariables.builder()
                    .variables(variables)
                    .build();
        } catch (Exception e) {
            logger.error("Error retrieving task variables for task: {}", taskId, e);
            return ProcessVariables.builder()
                    .variables(new HashMap<>())
                    .build();
        }
    }

    @Override
    public List<HistoricProcessInstance> getHistoricProcesses() {
        try {
            List<org.flowable.engine.history.HistoricProcessInstance> historicInstances =
                    historyService.createHistoricProcessInstanceQuery()
                            .finished()
                            .orderByProcessInstanceEndTime()
                            .desc()
                            .list();

            return historicInstances.stream()
                    .map(this::mapToHistoricProcessInstance)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error retrieving historic processes", e);
            return Collections.emptyList();
        }
    }

    @Override
    public ProcessVariables getHistoricProcessVariables(String processInstanceId) {
        try {
            List<HistoricVariableInstance> variables = historyService.createHistoricVariableInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .list();

            Map<String, Object> variableMap = variables.stream()
                    .collect(Collectors.toMap(
                            HistoricVariableInstance::getVariableName,
                            HistoricVariableInstance::getValue
                    ));

            return ProcessVariables.builder()
                    .variables(variableMap)
                    .build();
        } catch (Exception e) {
            logger.error("Error retrieving historic process variables for instance: {}", processInstanceId, e);
            return ProcessVariables.builder()
                    .variables(new HashMap<>())
                    .build();
        }
    }

    @Override
    public HistoricProcessInstance getHistoricProcessInstance(String processInstanceId) {
        try {
            org.flowable.engine.history.HistoricProcessInstance historicInstance =
                    historyService.createHistoricProcessInstanceQuery()
                            .processInstanceId(processInstanceId)
                            .singleResult();

            return historicInstance != null ? mapToHistoricProcessInstance(historicInstance) : null;
        } catch (Exception e) {
            logger.error("Error retrieving historic process instance: {}", processInstanceId, e);
            return null;
        }
    }

    @Override
    public ProcessHistory getProcessHistory(String processInstanceId) {
        try {
            org.flowable.engine.history.HistoricProcessInstance historicInstance =
                    historyService.createHistoricProcessInstanceQuery()
                            .processInstanceId(processInstanceId)
                            .singleResult();

            if (historicInstance == null) {
                return null;
            }

            return ProcessHistory.builder()
                    .historyId(historicInstance.getId())
                    .processId(processInstanceId)
                    .processDefinitionId(historicInstance.getProcessDefinitionId())
                    .startTime(LocalDateTime.ofInstant(historicInstance.getStartTime().toInstant(), ZoneId.systemDefault()))
                    .endTime(historicInstance.getEndTime() != null ?
                            LocalDateTime.ofInstant(historicInstance.getEndTime().toInstant(), ZoneId.systemDefault()) : null)
                    .durationInMillis(historicInstance.getDurationInMillis())
                    .build();
        } catch (Exception e) {
            logger.error("Error retrieving process history for instance: {}", processInstanceId, e);
            return null;
        }
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
        try {
            org.flowable.engine.runtime.ProcessInstance instance = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .singleResult();
            return instance != null;
        } catch (Exception e) {
            logger.error("Error checking if process is active: {}", processInstanceId, e);
            return false;
        }
    }

    @Override
    public ProcessInstance replayProcess(String processInstanceId, ProcessVariables variables) {
        try {
            org.flowable.engine.history.HistoricProcessInstance historicInstance =
                    historyService.createHistoricProcessInstanceQuery()
                            .processInstanceId(processInstanceId)
                            .singleResult();

            if (historicInstance == null) {
                throw new RuntimeException("Historic process instance not found: " + processInstanceId);
            }

            Map<String, Object> flowableVariables = variables != null ? variables.getVariables() : new HashMap<>();
            org.flowable.engine.runtime.ProcessInstance newInstance = runtimeService.startProcessInstanceByKey(
                    historicInstance.getProcessDefinitionKey(),
                    flowableVariables
            );

            logger.info("Replayed process instance: {} as new instance: {}", processInstanceId, newInstance.getId());

            return mapToProcessInstance(newInstance);
        } catch (Exception e) {
            logger.error("Failed to replay process: {}", processInstanceId, e);
            throw new RuntimeException("Failed to replay process: " + processInstanceId, e);
        }
    }

    private ProcessDefinition mapToProcessDefinition(org.flowable.engine.repository.ProcessDefinition flowableDef) {
        return ProcessDefinition.builder()
                .id(flowableDef.getId())
                .key(flowableDef.getKey())
                .name(flowableDef.getName())
                .version(flowableDef.getVersion())
                .deploymentId(flowableDef.getDeploymentId())
                .build();
    }

    private ProcessInstance mapToProcessInstance(org.flowable.engine.runtime.ProcessInstance flowableInstance) {
        return ProcessInstance.builder()
                .id(flowableInstance.getId())
                .processDefinitionId(flowableInstance.getProcessDefinitionId())
                .businessKey(flowableInstance.getBusinessKey())
                .status(flowableInstance.isEnded() ? "completed" : "active")
                .startTime(LocalDateTime.ofInstant(flowableInstance.getStartTime().toInstant(), ZoneId.systemDefault()))
                .build();
    }

    private DeploymentInfo mapToDeploymentInfo(Deployment deployment) {
        return DeploymentInfo.builder()
                .id(deployment.getId())
                .name(deployment.getName())
                .deploymentTime(LocalDateTime.ofInstant(deployment.getDeploymentTime().toInstant(), ZoneId.systemDefault()))
                .build();
    }

    private TaskInfo mapToTaskInfo(Task task) {
        return TaskInfo.builder()
                .taskId(task.getId())
                .name(task.getName())
                .description(task.getDescription())
                .assignee(task.getAssignee())
                .processId(task.getProcessInstanceId())
                .createTime(LocalDateTime.ofInstant(task.getCreateTime().toInstant(), ZoneId.systemDefault()))
                .build();
    }

    private HistoricProcessInstance mapToHistoricProcessInstance(org.flowable.engine.history.HistoricProcessInstance historicInstance) {
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