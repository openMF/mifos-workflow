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
import org.mifos.workflow.process.core.usecase.MifosProcessCompleteUsecase;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/// Completes a Camunda user task (a `userTask` with the `zeebe:userTask` extension).
@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnProperty(name = MIFOS_WORKFLOW_PROCESS_PROPERTIES_ENGINE, havingValue = "CAMUNDA")
final class CamundaCompleteUsecase implements MifosProcessCompleteUsecase {
    private final CamundaClient client;

    @Override
    public Response execute(Request request) {
        var key = CamundaIds.toKey(request.getTaskId());

        client.newCompleteUserTaskCommand(key)
                .variables(Objects.requireNonNullElseGet(request.getVariables(), Map::<String, Object>of))
                .send()
                .join();

        log.debug("completed task {}", key);

        return Response.builder().id(CamundaIds.toUuid(key)).build();
    }
}
