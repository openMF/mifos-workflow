///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.support.flowableremote.core.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

/// The part of the Flowable process REST API (`/service` below the root of the Flowable REST app) that the usecases
/// use.
///
/// Only the fields the usecases read are declared; the engine sends more, which are ignored. Queries return pages;
/// `FlowableRemotePages` reads all of them. Dates stay text; `FlowableRemoteMapping` parses them.
@HttpExchange(url = "/service", accept = MediaType.APPLICATION_JSON_VALUE)
public interface FlowableRemoteClient {
    @PostExchange(url = "/repository/deployments", contentType = MediaType.MULTIPART_FORM_DATA_VALUE)
    Deployment createDeployment(@RequestBody MultiValueMap<String, HttpEntity<?>> parts);

    @DeleteExchange("/repository/deployments/{id}")
    void deleteDeployment(@PathVariable String id, @RequestParam boolean cascade);

    @PostExchange(url = "/runtime/process-instances", contentType = MediaType.APPLICATION_JSON_VALUE)
    ProcessInstance startProcessInstance(@RequestBody Start body);

    @DeleteExchange("/runtime/process-instances/{id}")
    void deleteProcessInstance(@PathVariable String id, @RequestParam(required = false) @Nullable String deleteReason);

    @GetExchange("/runtime/executions")
    Page<Execution> executions(
            @RequestParam String processInstanceId,
            @RequestParam String signalEventSubscriptionName,
            @RequestParam int start,
            @RequestParam int size);

    /// Delivers a signal to one execution that waits for it.
    @PutExchange(url = "/runtime/executions/{id}", contentType = MediaType.APPLICATION_JSON_VALUE)
    void executionAction(@PathVariable String id, @RequestBody ExecutionAction body);

    /// Delivers a signal to all executions that wait for it.
    @PostExchange(url = "/runtime/signals", contentType = MediaType.APPLICATION_JSON_VALUE)
    void signal(@RequestBody Signal body);

    @GetExchange("/runtime/tasks")
    Page<Task> tasks(
            @RequestParam(required = false) @Nullable String assignee, @RequestParam int start, @RequestParam int size);

    @PostExchange(url = "/runtime/tasks/{id}", contentType = MediaType.APPLICATION_JSON_VALUE)
    void taskAction(@PathVariable String id, @RequestBody TaskAction body);

    /// Call with `finished=true`, `sort=endTime` and `order=desc` for the order of the embedded module.
    @GetExchange("/history/historic-process-instances")
    Page<HistoricProcessInstance> historicProcessInstances(
            @RequestParam boolean finished,
            @RequestParam String sort,
            @RequestParam String order,
            @RequestParam(required = false) @Nullable String processInstanceId,
            @RequestParam int start,
            @RequestParam int size);

    @GetExchange("/history/historic-process-instances/{id}")
    HistoricProcessInstance historicProcessInstance(@PathVariable String id);

    @GetExchange("/repository/process-definitions/{id}")
    ProcessDefinition processDefinition(@PathVariable String id);

    @GetExchange("/history/historic-variable-instances")
    Page<HistoricVariableInstance> historicVariableInstances(
            @RequestParam String processInstanceId, @RequestParam int start, @RequestParam int size);

    /// A typed variable as the REST API expects it, for example `{"name": "amount", "type": "integer", "value": 42}`.
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    record Variable(
            String name, @Nullable String type, @Nullable Object value) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Page<T>(List<T> data, long total, int start, int size) {}

    record Start(String processDefinitionKey, List<Variable> variables) {}

    record ExecutionAction(String action, String signalName, List<Variable> variables) {
        public static ExecutionAction signalEventReceived(String signalName, List<Variable> variables) {
            return new ExecutionAction("signalEventReceived", signalName, variables);
        }
    }

    record Signal(String signalName, List<Variable> variables) {}

    record TaskAction(String action, List<Variable> variables) {
        public static TaskAction complete(List<Variable> variables) {
            return new TaskAction("complete", variables);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Deployment(String id, @Nullable String name) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record ProcessInstance(String id, @Nullable String processDefinitionId) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Execution(String id) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record ProcessDefinition(String id, String key) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Task(
            String id,
            @Nullable String name,
            @Nullable String assignee,
            @Nullable String createTime,
            @Nullable String dueDate,
            @Nullable String description,
            @Nullable Integer priority,
            @Nullable String processInstanceId,
            @Nullable String processDefinitionId) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record HistoricProcessInstance(
            String id,
            @Nullable String processDefinitionId,
            @Nullable String processDefinitionKey,
            @Nullable String startTime,
            @Nullable String endTime,
            @Nullable Long durationInMillis,
            @Nullable String startUserId,
            @Nullable String deleteReason) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record HistoricVariableInstance(Variable variable) {}
}
