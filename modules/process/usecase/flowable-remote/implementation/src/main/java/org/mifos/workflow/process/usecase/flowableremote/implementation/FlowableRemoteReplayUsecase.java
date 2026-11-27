///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.usecase.flowableremote.implementation;

import static org.mifos.workflow.process.core.MifosProcessConstants.MIFOS_WORKFLOW_PROCESS_PROPERTIES_ENGINE;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mifos.workflow.process.core.usecase.MifosProcessReplayUsecase;
import org.mifos.workflow.process.support.flowableremote.core.client.FlowableRemoteClient;
import org.mifos.workflow.process.support.flowableremote.core.client.FlowableRemoteClient.Variable;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/// Starts the definition of a finished or running process again with its variables.
///
/// The source variables are sent back with the type the engine reports, so they keep their type.
@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnProperty(name = MIFOS_WORKFLOW_PROCESS_PROPERTIES_ENGINE, havingValue = "FLOWABLE_REMOTE")
final class FlowableRemoteReplayUsecase implements MifosProcessReplayUsecase {
    private final FlowableRemoteClient client;
    private final FlowableRemoteVariables variables;

    @Override
    public Response execute(Request request) {
        var sourceId = request.getSourceProcessId().toString();
        var source = client.historicProcessInstance(sourceId);

        var replayVariables = new LinkedHashMap<String, Variable>();
        FlowableRemotePages.all((start, size) -> client.historicVariableInstances(sourceId, start, size))
                .forEach(v -> replayVariables.put(v.variable().name(), v.variable()));
        variables.toTyped(request.getVariables()).forEach(v -> replayVariables.put(v.name(), v));

        var key = Objects.requireNonNull(
                FlowableRemoteMapping.processDefinitionKey(
                        source, id -> client.processDefinition(id).key()),
                "process definition key of " + sourceId);
        var replayed = client.startProcessInstance(
                new FlowableRemoteClient.Start(key, new ArrayList<>(replayVariables.values())));

        log.debug("replayed process {} as {}", sourceId, replayed.id());

        return Response.builder().id(FlowableIds.toUuid(replayed.id())).build();
    }
}
