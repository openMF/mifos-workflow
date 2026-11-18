///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.usecase.camunda.implementation;

import static org.mifos.workflow.process.core.MifosProcessConstants.MIFOS_WORKFLOW_PROCESS_PROPERTIES_ENGINE;

import io.camunda.client.CamundaClient;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mifos.workflow.process.core.usecase.MifosProcessSignalUsecase;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/// Broadcasts a signal to every waiting process. Camunda 8 signals cannot target one process instance, so a request
/// with an id is rejected rather than silently broadcast to all of them; use a message correlated by key instead.
@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnProperty(name = MIFOS_WORKFLOW_PROCESS_PROPERTIES_ENGINE, havingValue = "CAMUNDA")
final class CamundaSignalUsecase implements MifosProcessSignalUsecase {
    private final CamundaClient client;

    @Override
    public Response execute(Request request) {
        if (request.getId() != null) {
            throw new UnsupportedOperationException("Camunda 8 can only broadcast signals; signal '"
                    + request.getSignalName() + "' cannot be sent to process " + request.getId() + " alone");
        }

        client.newBroadcastSignalCommand()
                .signalName(request.getSignalName())
                .variables(Objects.requireNonNullElseGet(request.getVariables(), Map::<String, Object>of))
                .send()
                .join();

        log.debug("broadcast signal {}", request.getSignalName());

        return Response.builder().build();
    }
}
