///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.usecase.camunda.implementation;

import static org.mifos.workflow.process.core.MifosProcessConstants.MIFOS_WORKFLOW_PROCESS_PROPERTIES_ENGINE;

import io.camunda.client.CamundaClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mifos.workflow.process.core.usecase.MifosProcessTerminateUsecase;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/// Cancels a process instance. Camunda 8 does not record a cancellation reason, so the reason is only logged.
@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnProperty(name = MIFOS_WORKFLOW_PROCESS_PROPERTIES_ENGINE, havingValue = "CAMUNDA")
final class CamundaTerminateUsecase implements MifosProcessTerminateUsecase {
    private final CamundaClient client;

    @Override
    public Response execute(Request request) {
        var key = CamundaIds.toKey(request.getProcessId());

        client.newCancelInstanceCommand(key).send().join();

        log.debug("terminated process {}: {}", key, request.getReason());

        return Response.builder().id(CamundaIds.toUuid(key)).build();
    }
}
