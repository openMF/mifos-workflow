///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.usecase.flowableremote.implementation;

import static org.mifos.workflow.process.core.MifosProcessConstants.MIFOS_WORKFLOW_PROCESS_PROPERTIES_ENGINE;

import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mifos.workflow.process.core.usecase.MifosProcessHistoryUsecase;
import org.mifos.workflow.process.support.flowableremote.core.client.FlowableRemoteClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnProperty(name = MIFOS_WORKFLOW_PROCESS_PROPERTIES_ENGINE, havingValue = "FLOWABLE_REMOTE")
final class FlowableRemoteHistoryUsecase implements MifosProcessHistoryUsecase {
    private final FlowableRemoteClient client;

    @Override
    public Response execute(Request request) {
        var processId = request.getId() == null ? null : request.getId().toString();
        var instances = FlowableRemotePages.all(
                (start, size) -> client.historicProcessInstances(true, "endTime", "desc", processId, start, size));

        log.debug("found {} historic process instances", instances.size());

        // one lookup per process definition, not per process instance
        var definitionKeys = new HashMap<String, String>();
        return Response.builder()
                .entries(FlowableRemoteMapping.toEntries(
                        instances,
                        id -> definitionKeys.computeIfAbsent(
                                id,
                                definitionId ->
                                        client.processDefinition(definitionId).key())))
                .build();
    }
}
