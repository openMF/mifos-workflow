///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.usecase.cibsevenremote.implementation;

import static org.mifos.workflow.process.core.MifosProcessConstants.MIFOS_WORKFLOW_PROCESS_PROPERTIES_ENGINE;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mifos.workflow.process.core.usecase.MifosProcessTerminateUsecase;
import org.mifos.workflow.process.support.cibsevenremote.core.client.CibsevenRemoteClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/// Deletes a running process instance.
///
/// The synchronous REST call has no parameter for a reason, so the reason is only logged; the history shows no
/// delete reason. This is the same limit as for Camunda 8.
@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnProperty(name = MIFOS_WORKFLOW_PROCESS_PROPERTIES_ENGINE, havingValue = "CIBSEVEN_REMOTE")
final class CibsevenRemoteTerminateUsecase implements MifosProcessTerminateUsecase {
    private final CibsevenRemoteClient client;

    @Override
    public Response execute(Request request) {
        client.deleteProcessInstance(request.getProcessId());

        log.debug("terminated process {}: {}", request.getProcessId(), request.getReason());

        return Response.builder().id(CibsevenIds.toUuid(request.getProcessId())).build();
    }
}
