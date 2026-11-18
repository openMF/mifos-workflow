///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.usecase.cibsevenremote.implementation;

import static org.mifos.workflow.process.core.MifosProcessConstants.MIFOS_WORKFLOW_PROCESS_PROPERTIES_ENGINE;

import java.util.LinkedHashMap;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mifos.workflow.process.core.usecase.MifosProcessReplayUsecase;
import org.mifos.workflow.process.support.cibsevenremote.core.client.CibsevenRemoteClient;
import org.mifos.workflow.process.support.cibsevenremote.core.client.CibsevenRemoteClient.VariableValue;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/// Starts the definition of a finished or running process again with its variables.
///
/// The source variables are sent back in their stored form, so object and JSON values keep their type.
@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnProperty(name = MIFOS_WORKFLOW_PROCESS_PROPERTIES_ENGINE, havingValue = "CIBSEVEN_REMOTE")
final class CibsevenRemoteReplayUsecase implements MifosProcessReplayUsecase {
    private final CibsevenRemoteClient client;
    private final CibsevenRemoteVariables variables;

    @Override
    public Response execute(Request request) {
        var sourceId = request.getSourceProcessId().toString();
        var source = client.historicProcessInstance(sourceId);

        var replayVariables = new LinkedHashMap<String, VariableValue>();
        client.historicVariableInstances(sourceId, false)
                .forEach(v -> replayVariables.put(v.name(), new VariableValue(v.value(), v.type(), v.valueInfo())));
        replayVariables.putAll(variables.toTyped(request.getVariables()));

        var key = Objects.requireNonNull(source.processDefinitionKey(), "process definition key of " + sourceId);
        var replayed = client.startProcessInstance(key, new CibsevenRemoteClient.Variables(replayVariables));

        log.debug("replayed process {} as {}", sourceId, replayed.id());

        return Response.builder().id(CibsevenIds.toUuid(replayed.id())).build();
    }
}
