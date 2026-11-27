///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.usecase.camunda.implementation;

import static org.mifos.workflow.process.core.MifosProcessConstants.MIFOS_WORKFLOW_PROCESS_PROPERTIES_ENGINE;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.search.response.Variable;
import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mifos.workflow.process.core.usecase.MifosProcessReplayUsecase;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/// Starts a new instance of the source process's definition (latest version) with the source's process-level
/// variables, overridden by the request's. Source and variables come from Camunda's search API, so a source that
/// finished an instant ago may not be visible yet.
@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnProperty(name = MIFOS_WORKFLOW_PROCESS_PROPERTIES_ENGINE, havingValue = "CAMUNDA")
final class CamundaReplayUsecase implements MifosProcessReplayUsecase {
    private final CamundaClient client;

    @Override
    public Response execute(Request request) {
        var sourceKey = CamundaIds.toKey(request.getSourceProcessId());
        var source = client.newProcessInstanceGetRequest(sourceKey).send().join();

        var variables = new HashMap<String, Object>();
        client.newVariableSearchRequest()
                .filter(filter -> filter.processInstanceKey(sourceKey).scopeKey(sourceKey))
                .send()
                .join()
                .items()
                .forEach(variable -> variables.put(variable.getName(), value(variable)));

        if (request.getVariables() != null) {
            variables.putAll(request.getVariables());
        }

        var replayed = client.newCreateInstanceCommand()
                .bpmnProcessId(source.getProcessDefinitionId())
                .latestVersion()
                .variables(variables)
                .send()
                .join();

        log.debug("replayed process {} as {}", sourceKey, replayed.getProcessInstanceKey());

        return Response.builder()
                .id(CamundaIds.toUuid(replayed.getProcessInstanceKey()))
                .build();
    }

    /// Search results carry variable values as JSON text, truncated when large.
    private Object value(Variable variable) {
        var json = Boolean.TRUE.equals(variable.isTruncated())
                ? client.newVariableGetRequest(variable.getVariableKey())
                        .send()
                        .join()
                        .getValue()
                : variable.getValue();

        return client.getConfiguration().getJsonMapper().fromJson(json, Object.class);
    }
}
