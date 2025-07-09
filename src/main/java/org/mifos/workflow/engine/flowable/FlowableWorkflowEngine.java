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
        } catch (IllegalArgumentException e) {
            logger.error("Invalid arguments provided for Flowable ProcessEngine initialization", e);
            throw new IllegalArgumentException("Invalid arguments for Flowable ProcessEngine initialization", e);
        } catch (IllegalStateException e) {
            logger.error("Invalid state during Flowable ProcessEngine initialization", e);
            throw new IllegalStateException("Invalid state during Flowable ProcessEngine initialization", e);
        } catch (RuntimeException e) {
            logger.error("Runtime error during Flowable ProcessEngine initialization", e);
            throw new RuntimeException("Runtime error during Flowable ProcessEngine initialization", e);
        }
    }

    @Override
    public List<ProcessDefinition> getProcessDefinitions() {
        try {
            List<org.flowable.engine.repository.ProcessDefinition> flowableDefinitions = repositoryService.createProcessDefinitionQuery()
                    .active()
                    .list();

            return flowableDefinitions.stream()
                    .map(flowableMapper::mapToProcessDefinition)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            logger.error("Invalid arguments provided for retrieving process definitions", e);
            throw new IllegalArgumentException("Invalid arguments for retrieving process definitions", e);
        } catch (IllegalStateException e) {
            logger.error("Invalid state while retrieving process definitions", e);
            throw new IllegalStateException("Invalid state while retrieving process definitions", e);
        } catch (RuntimeException e) {
            logger.error("Runtime error while retrieving process definitions", e);
            throw new RuntimeException("Runtime error while retrieving process definitions", e);
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
        } catch (IllegalArgumentException e) {
            logger.error("Invalid arguments provided for process deployment: {}", filename, e);
            return DeploymentResult.builder()
                    .deploymentId(null)
                    .name(filename)
                    .deploymentTime(LocalDateTime.now())
                    .success(false)
                    .errors(Collections.singletonList("Invalid arguments for process deployment: " + e.getMessage()))
                    .build();
        } catch (IllegalStateException e) {
            logger.error("Invalid state during process deployment: {}", filename, e);
            return DeploymentResult.builder()
                    .deploymentId(null)
                    .name(filename)
                    .deploymentTime(LocalDateTime.now())
                    .success(false)
                    .errors(Collections.singletonList("Invalid state during process deployment: " + e.getMessage()))
                    .build();
        } catch (RuntimeException e) {
            logger.error("Runtime error during process deployment: {}", filename, e);
        return DeploymentResult.builder()
                    .deploymentId(null)
                .name(filename)
                .deploymentTime(LocalDateTime.now())
                .success(false)
                    .errors(Collections.singletonList("Runtime error during process deployment: " + e.getMessage()))
                .build();
        }
    }

    @Override
    public void deleteDeployment(String deploymentId) {
        try {
            repositoryService.deleteDeployment(deploymentId, true);
            logger.info("Successfully deleted deployment: {}", deploymentId);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid arguments provided for deployment deletion: {}", deploymentId, e);
            throw new IllegalArgumentException("Invalid arguments for deployment deletion: " + deploymentId, e);
        } catch (IllegalStateException e) {
            logger.error("Invalid state during deployment deletion: {}", deploymentId, e);
            throw new IllegalStateException("Invalid state during deployment deletion: " + deploymentId, e);
        } catch (RuntimeException e) {
            logger.error("Runtime error during deployment deletion: {}", deploymentId, e);
            throw new RuntimeException("Runtime error during deployment deletion: " + deploymentId, e);
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
                    .map(flowableMapper::mapToDeploymentInfo)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            logger.error("Invalid arguments provided for retrieving deployments", e);
            throw new IllegalArgumentException("Invalid arguments for retrieving deployments", e);
        } catch (IllegalStateException e) {
            logger.error("Invalid state while retrieving deployments", e);
            throw new IllegalStateException("Invalid state while retrieving deployments", e);
        } catch (RuntimeException e) {
            logger.error("Runtime error while retrieving deployments", e);
            throw new RuntimeException("Runtime error while retrieving deployments", e);
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

            return flowableMapper.mapToProcessInstance(flowableInstance);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid arguments provided for process start: {}", processDefinitionKey, e);
            throw new IllegalArgumentException("Invalid arguments for process start: " + processDefinitionKey, e);
        } catch (IllegalStateException e) {
            logger.error("Invalid state during process start: {}", processDefinitionKey, e);
            throw new IllegalStateException("Invalid state during process start: " + processDefinitionKey, e);
        } catch (RuntimeException e) {
            logger.error("Runtime error during process start: {}", processDefinitionKey, e);
            throw new RuntimeException("Runtime error during process start: " + processDefinitionKey, e);
        }
    }

    @Override
    public List<ProcessInstance> getProcessInstances() {
        try {
            List<org.flowable.engine.runtime.ProcessInstance> flowableInstances = runtimeService.createProcessInstanceQuery()
                    .active()
                    .list();

            return flowableInstances.stream()
                    .map(flowableMapper::mapToProcessInstance)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            logger.error("Invalid arguments provided for retrieving process instances", e);
            throw new IllegalArgumentException("Invalid arguments for retrieving process instances", e);
        } catch (IllegalStateException e) {
            logger.error("Invalid state while retrieving process instances", e);
            throw new IllegalStateException("Invalid state while retrieving process instances", e);
        } catch (RuntimeException e) {
            logger.error("Runtime error while retrieving process instances", e);
            throw new RuntimeException("Runtime error while retrieving process instances", e);
        }
    }

    @Override
    public ProcessVariables getProcessVariables(String processInstanceId) {
        try {
            Map<String, Object> variables = runtimeService.getVariables(processInstanceId);
            return ProcessVariables.builder()
                    .variables(variables)
                    .build();
        } catch (IllegalArgumentException e) {
            logger.error("Invalid arguments provided for retrieving process variables: {}", processInstanceId, e);
            return ProcessVariables.builder()
                    .variables(new HashMap<>())
                    .build();
        } catch (IllegalStateException e) {
            logger.error("Invalid state while retrieving process variables: {}", processInstanceId, e);
            return ProcessVariables.builder()
                    .variables(new HashMap<>())
                    .build();
        } catch (RuntimeException e) {
            logger.error("Runtime error while retrieving process variables: {}", processInstanceId, e);
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
        } catch (IllegalArgumentException e) {
            logger.error("Invalid arguments provided for task completion: {}", taskId, e);
            throw new IllegalArgumentException("Invalid arguments for task completion: " + taskId, e);
        } catch (IllegalStateException e) {
            logger.error("Invalid state during task completion: {}", taskId, e);
            throw new IllegalStateException("Invalid state during task completion: " + taskId, e);
        } catch (RuntimeException e) {
            logger.error("Runtime error during task completion: {}", taskId, e);
            throw new RuntimeException("Runtime error during task completion: " + taskId, e);
        }
    }

    @Override
    public List<TaskInfo> getPendingTasks(String userId) {
        try {
            List<Task> tasks = taskService.createTaskQuery()
                    .taskAssignee(userId)
                    .list();

            return tasks.stream()
                    .map(flowableMapper::mapToTaskInfo)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            logger.error("Invalid arguments provided for retrieving pending tasks for user: {}", userId, e);
            throw new IllegalArgumentException("Invalid arguments for retrieving pending tasks for user: " + userId, e);
        } catch (IllegalStateException e) {
            logger.error("Invalid state while retrieving pending tasks for user: {}", userId, e);
            throw new IllegalStateException("Invalid state while retrieving pending tasks for user: " + userId, e);
        } catch (RuntimeException e) {
            logger.error("Runtime error while retrieving pending tasks for user: {}", userId, e);
            throw new RuntimeException("Runtime error while retrieving pending tasks for user: " + userId, e);
        }
    }

    @Override
    public List<TaskInfo> getPendingTasksForProcess(String processInstanceId) {
        try {
            List<Task> tasks = taskService.createTaskQuery()
                    .processInstanceId(processInstanceId)
                    .list();

            return tasks.stream()
                    .map(flowableMapper::mapToTaskInfo)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            logger.error("Invalid arguments provided for retrieving pending tasks for process: {}", processInstanceId, e);
            throw new IllegalArgumentException("Invalid arguments for retrieving pending tasks for process: " + processInstanceId, e);
        } catch (IllegalStateException e) {
            logger.error("Invalid state while retrieving pending tasks for process: {}", processInstanceId, e);
            throw new IllegalStateException("Invalid state while retrieving pending tasks for process: " + processInstanceId, e);
        } catch (RuntimeException e) {
            logger.error("Runtime error while retrieving pending tasks for process: {}", processInstanceId, e);
            throw new RuntimeException("Runtime error while retrieving pending tasks for process: " + processInstanceId, e);
        }
    }

    @Override
    public ProcessVariables getTaskVariables(String taskId) {
        try {
            Map<String, Object> variables = taskService.getVariables(taskId);
            return ProcessVariables.builder()
                    .variables(variables)
                    .build();
        } catch (IllegalArgumentException e) {
            logger.error("Invalid arguments provided for retrieving task variables: {}", taskId, e);
            return ProcessVariables.builder()
                    .variables(new HashMap<>())
                    .build();
        } catch (IllegalStateException e) {
            logger.error("Invalid state while retrieving task variables: {}", taskId, e);
            return ProcessVariables.builder()
                    .variables(new HashMap<>())
                    .build();
        } catch (RuntimeException e) {
            logger.error("Runtime error while retrieving task variables: {}", taskId, e);
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
                    .map(flowableMapper::mapToHistoricProcessInstance)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            logger.error("Invalid arguments provided for retrieving historic processes", e);
            throw new IllegalArgumentException("Invalid arguments for retrieving historic processes", e);
        } catch (IllegalStateException e) {
            logger.error("Invalid state while retrieving historic processes", e);
            throw new IllegalStateException("Invalid state while retrieving historic processes", e);
        } catch (RuntimeException e) {
            logger.error("Runtime error while retrieving historic processes", e);
            throw new RuntimeException("Runtime error while retrieving historic processes", e);
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
        } catch (IllegalArgumentException e) {
            logger.error("Invalid arguments provided for retrieving historic process variables: {}", processInstanceId, e);
            return ProcessVariables.builder()
                    .variables(new HashMap<>())
                    .build();
        } catch (IllegalStateException e) {
            logger.error("Invalid state while retrieving historic process variables: {}", processInstanceId, e);
            return ProcessVariables.builder()
                    .variables(new HashMap<>())
                    .build();
        } catch (RuntimeException e) {
            logger.error("Runtime error while retrieving historic process variables: {}", processInstanceId, e);
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

            return historicInstance != null ? flowableMapper.mapToHistoricProcessInstance(historicInstance) : null;
        } catch (IllegalArgumentException e) {
            logger.error("Invalid arguments provided for retrieving historic process instance: {}", processInstanceId, e);
            return null;
        } catch (IllegalStateException e) {
            logger.error("Invalid state while retrieving historic process instance: {}", processInstanceId, e);
            return null;
        } catch (RuntimeException e) {
            logger.error("Runtime error while retrieving historic process instance: {}", processInstanceId, e);
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
        } catch (IllegalArgumentException e) {
            logger.error("Invalid arguments provided for retrieving process history: {}", processInstanceId, e);
            return null;
        } catch (IllegalStateException e) {
            logger.error("Invalid state while retrieving process history: {}", processInstanceId, e);
            return null;
        } catch (RuntimeException e) {
            logger.error("Runtime error while retrieving process history: {}", processInstanceId, e);
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
        } catch (IllegalArgumentException e) {
            logger.error("Invalid arguments provided for checking process active status: {}", processInstanceId, e);
            return false;
        } catch (IllegalStateException e) {
            logger.error("Invalid state while checking process active status: {}", processInstanceId, e);
            return false;
        } catch (RuntimeException e) {
            logger.error("Runtime error while checking process active status: {}", processInstanceId, e);
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

            return flowableMapper.mapToProcessInstance(newInstance);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid arguments provided for process replay: {}", processInstanceId, e);
            throw new IllegalArgumentException("Invalid arguments for process replay: " + processInstanceId, e);
        } catch (IllegalStateException e) {
            logger.error("Invalid state during process replay: {}", processInstanceId, e);
            throw new IllegalStateException("Invalid state during process replay: " + processInstanceId, e);
        } catch (RuntimeException e) {
            logger.error("Runtime error during process replay: {}", processInstanceId, e);
            throw new RuntimeException("Runtime error during process replay: " + processInstanceId, e);
        }
    }
}