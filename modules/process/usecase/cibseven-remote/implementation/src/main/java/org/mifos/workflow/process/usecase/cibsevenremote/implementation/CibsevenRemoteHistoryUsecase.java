///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.usecase.cibsevenremote.implementation;

import static org.mifos.workflow.process.core.MifosProcessConstants.MIFOS_WORKFLOW_PROCESS_PROPERTIES_ENGINE;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mifos.workflow.process.core.usecase.MifosProcessHistoryUsecase;
import org.mifos.workflow.process.support.cibsevenremote.core.client.CibsevenRemoteClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnProperty(name = MIFOS_WORKFLOW_PROCESS_PROPERTIES_ENGINE, havingValue = "CIBSEVEN_REMOTE")
final class CibsevenRemoteHistoryUsecase implements MifosProcessHistoryUsecase {
    private final CibsevenRemoteClient client;

    @Override
    public Response execute(Request request) {
        var processId = request.getId() == null ? null : request.getId().toString();
        var instances = client.historicProcessInstances(true, processId);

        log.debug("found {} history entries", instances.size());

        return Response.builder()
                .entries(CibsevenRemoteMapping.toEntries(instances))
                .build();
    }
}
