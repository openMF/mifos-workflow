///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.support.cibsevenremote.core.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.Map;
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

/// The part of the CIB seven REST API (`/engine-rest`) that the usecases use.
///
/// Only the fields the usecases read are declared; the engine sends more, which are ignored. Dates stay text,
/// because the engine writes offsets without a colon (`+0200`); `CibsevenRemoteMapping` parses them.
@HttpExchange(accept = MediaType.APPLICATION_JSON_VALUE)
public interface CibsevenRemoteClient {
    @PostExchange(url = "/deployment/create", contentType = MediaType.MULTIPART_FORM_DATA_VALUE)
    Deployment createDeployment(@RequestBody MultiValueMap<String, HttpEntity<?>> parts);

    @DeleteExchange("/deployment/{id}")
    void deleteDeployment(@PathVariable String id, @RequestParam boolean cascade);

    @PostExchange(url = "/process-definition/key/{key}/start", contentType = MediaType.APPLICATION_JSON_VALUE)
    ProcessInstance startProcessInstance(@PathVariable String key, @RequestBody Variables body);

    @DeleteExchange("/process-instance/{id}")
    void deleteProcessInstance(@PathVariable String id);

    @GetExchange("/execution")
    List<Execution> executions(
            @RequestParam String processInstanceId, @RequestParam String signalEventSubscriptionName);

    @PostExchange(url = "/signal", contentType = MediaType.APPLICATION_JSON_VALUE)
    void signal(@RequestBody Signal body);

    @GetExchange("/task")
    List<Task> tasks(@RequestParam(required = false) @Nullable String assignee);

    @PostExchange(url = "/task/{id}/complete", contentType = MediaType.APPLICATION_JSON_VALUE)
    void completeTask(@PathVariable String id, @RequestBody Variables body);

    @GetExchange("/history/process-instance")
    List<HistoricProcessInstance> historicProcessInstances(
            @RequestParam boolean finished, @RequestParam(required = false) @Nullable String processInstanceId);

    @GetExchange("/history/process-instance/{id}")
    HistoricProcessInstance historicProcessInstance(@PathVariable String id);

    /// With `deserializeValues=false` the engine returns object and JSON values in their stored form, so they can
    /// be sent back unchanged.
    @GetExchange("/history/variable-instance")
    List<HistoricVariableInstance> historicVariableInstances(
            @RequestParam String processInstanceId, @RequestParam boolean deserializeValues);

    /// A typed variable value as the REST API expects it, for example `{"value": 42, "type": "Integer"}`.
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    record VariableValue(
            @Nullable Object value,
            @Nullable String type,
            @Nullable Map<String, Object> valueInfo) {}

    record Variables(Map<String, VariableValue> variables) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    record Signal(String name, @Nullable String executionId, Map<String, VariableValue> variables) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Deployment(String id, @Nullable String name) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record ProcessInstance(String id, @Nullable String definitionId) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Execution(String id) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Task(
            String id,
            @Nullable String name,
            @Nullable String assignee,
            @Nullable String created,
            @Nullable String due,
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
    record HistoricVariableInstance(
            String name,
            @Nullable String type,
            @Nullable Object value,
            @Nullable Map<String, Object> valueInfo) {}
}
