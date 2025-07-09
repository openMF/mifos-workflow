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
import org.mifos.workflow.util.ExceptionHandler;
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
    private final FlowableMapper flowableMapper;
    private ProcessEngine processEngine;
    private RepositoryService repositoryService;
    private RuntimeService runtimeService;
    private TaskService taskService;
    private HistoryService historyService;

    @Autowired
    public FlowableWorkflowEngine(WorkflowConfig properties, DataSource dataSource, FlowableMapper flowableMapper) {
        this.properties = properties;
        this.flowableMapper = flowableMapper;
        initializeEngine(dataSource);
        logger.info("FlowableWorkflowEngine initialized successfully");
    }

    private void initializeEngine(DataSource dataSource) {
        ExceptionHandler.executeWithExceptionHandling("Flowable ProcessEngine initialization", "dataSource", () -> {
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

            logger.info("Flowable ProcessEngine initialized successfully. Database schema update: {}, Async executor: {}",
                    properties.getEngine().getFlowable().isDatabaseSchemaUpdate(),
                    properties.getEngine().getFlowable().isAsyncExecutorEnabled());
            return null;
        });
    }

    @Override
    public List<ProcessDefinition> getProcessDefinitions() {
        return ExceptionHandler.executeWithExceptionHandling("retrieving process definitions", "all", () -> {
            List<org.flowable.engine.repository.ProcessDefinition> flowableDefinitions = repositoryService.createProcessDefinitionQuery()
                    .active()
                    .list();

            return flowableDefinitions.stream()
                    .map(flowableMapper::mapToProcessDefinition)
                    .collect(Collectors.toList());
        });
    }

    @Override
    public DeploymentResult deployProcess(InputStream processDefinition, String filename) {
        return ExceptionHandler.executeWithExceptionHandling("process deployment", filename, () -> {
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
        });
    }

    @Override
    public void deleteDeployment(String deploymentId) {
        ExceptionHandler.executeWithExceptionHandling("deployment deletion", deploymentId, () -> {
            repositoryService.deleteDeployment(deploymentId, true);
            logger.info("Successfully deleted deployment: {}", deploymentId);
        });
    }

    @Override
    public List<DeploymentInfo> getDeployments() {
        return ExceptionHandler.executeWithExceptionHandling("retrieving deployments", "all", () -> {
            List<Deployment> deployments = repositoryService.createDeploymentQuery()
                    .orderByDeploymentTime()
                    .desc()
                    .list();

            return deployments.stream()
                    .map(flowableMapper::mapToDeploymentInfo)
                    .collect(Collectors.toList());
        });
    }

    @Override
    public ProcessInstance startProcess(String processDefinitionKey, ProcessVariables variables) {
        return ExceptionHandler.executeWithExceptionHandling("process start", processDefinitionKey, () -> {
            Map<String, Object> flowableVariables = variables != null ? variables.getVariables() : new HashMap<>();

            org.flowable.engine.runtime.ProcessInstance flowableInstance = runtimeService.startProcessInstanceByKey(
                    processDefinitionKey,
                    flowableVariables
            );

            logger.info("Started process instance: {} for definition: {}",
                    flowableInstance.getId(), processDefinitionKey);

            return flowableMapper.mapToProcessInstance(flowableInstance);
        });
    }

    @Override
    public List<ProcessInstance> getProcessInstances() {
        return ExceptionHandler.executeWithExceptionHandling("retrieving process instances", "all", () -> {
            List<org.flowable.engine.runtime.ProcessInstance> flowableInstances = runtimeService.createProcessInstanceQuery()
                    .active()
                    .list();

            return flowableInstances.stream()
                    .map(flowableMapper::mapToProcessInstance)
                    .collect(Collectors.toList());
        });
    }

    @Override
    public ProcessVariables getProcessVariables(String processInstanceId) {
        return ExceptionHandler.executeWithExceptionHandling("retrieving process variables", processInstanceId, () -> {
            Map<String, Object> variables = runtimeService.getVariables(processInstanceId);
            return ProcessVariables.builder()
                    .variables(variables)
                    .build();
        });
    }

    @Override
    public void completeTask(String taskId, ProcessVariables variables) {
        ExceptionHandler.executeWithExceptionHandling("task completion", taskId, () -> {
            Map<String, Object> flowableVariables = variables != null ? variables.getVariables() : new HashMap<>();
            taskService.complete(taskId, flowableVariables);
            logger.info("Completed task: {}", taskId);
        });
    }

    @Override
    public List<TaskInfo> getPendingTasks(String userId) {
        return ExceptionHandler.executeWithExceptionHandling("retrieving pending tasks for user", userId, () -> {
            List<Task> tasks = taskService.createTaskQuery()
                    .taskAssignee(userId)
                    .list();

            return tasks.stream()
                    .map(flowableMapper::mapToTaskInfo)
                    .collect(Collectors.toList());
        });
    }

    @Override
    public List<TaskInfo> getPendingTasksForProcess(String processInstanceId) {
        return ExceptionHandler.executeWithExceptionHandling("retrieving pending tasks for process", processInstanceId, () -> {
            List<Task> tasks = taskService.createTaskQuery()
                    .processInstanceId(processInstanceId)
                    .list();

            return tasks.stream()
                    .map(flowableMapper::mapToTaskInfo)
                    .collect(Collectors.toList());
        });
    }

    @Override
    public ProcessVariables getTaskVariables(String taskId) {
        return ExceptionHandler.executeWithExceptionHandling("retrieving task variables", taskId, () -> {
            Map<String, Object> variables = taskService.getVariables(taskId);
            return ProcessVariables.builder()
                    .variables(variables)
                    .build();
        });
    }

    @Override
    public List<HistoricProcessInstance> getHistoricProcesses() {
        return ExceptionHandler.executeWithExceptionHandling("retrieving historic processes", "all", () -> {
            List<org.flowable.engine.history.HistoricProcessInstance> historicInstances =
                    historyService.createHistoricProcessInstanceQuery()
                            .finished()
                            .orderByProcessInstanceEndTime()
                            .desc()
                            .list();

            return historicInstances.stream()
                    .map(flowableMapper::mapToHistoricProcessInstance)
                    .collect(Collectors.toList());
        });
    }

    @Override
    public ProcessVariables getHistoricProcessVariables(String processInstanceId) {
        return ExceptionHandler.executeWithExceptionHandling("retrieving historic process variables", processInstanceId, () -> {
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
        });
    }

    @Override
    public HistoricProcessInstance getHistoricProcessInstance(String processInstanceId) {
        return ExceptionHandler.executeWithExceptionHandling("retrieving historic process instance", processInstanceId, () -> {
            org.flowable.engine.history.HistoricProcessInstance historicInstance =
                    historyService.createHistoricProcessInstanceQuery()
                            .processInstanceId(processInstanceId)
                            .singleResult();

            return historicInstance != null ? flowableMapper.mapToHistoricProcessInstance(historicInstance) : null;
        });
    }

    @Override
    public ProcessHistory getProcessHistory(String processInstanceId) {
        return ExceptionHandler.executeWithExceptionHandling("retrieving process history", processInstanceId, () -> {
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
        return ExceptionHandler.executeWithExceptionHandling("checking process active status", processInstanceId, () -> {
            org.flowable.engine.runtime.ProcessInstance instance = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .singleResult();
            return instance != null;
        });
    }

    @Override
    public ProcessInstance replayProcess(String processInstanceId, ProcessVariables variables) {
        return ExceptionHandler.executeWithExceptionHandling("process replay", processInstanceId, () -> {
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

            return flowableMapper.mapToProcessInstance(newInstance);
        });
    }
}