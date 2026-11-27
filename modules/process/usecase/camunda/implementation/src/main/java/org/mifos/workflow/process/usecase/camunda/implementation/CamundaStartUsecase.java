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
import org.mifos.workflow.process.core.usecase.MifosProcessStartUsecase;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnProperty(name = MIFOS_WORKFLOW_PROCESS_PROPERTIES_ENGINE, havingValue = "CAMUNDA")
final class CamundaStartUsecase implements MifosProcessStartUsecase {
    private final CamundaClient client;

    @Override
    public Response execute(Request request) {
        var instance = client.newCreateInstanceCommand()
                .bpmnProcessId(request.getKey())
                .latestVersion()
                .variables(Objects.requireNonNullElseGet(request.getVariables(), Map::<String, Object>of))
                .send()
                .join();

        log.debug("started process {} with instance key {}", request.getKey(), instance.getProcessInstanceKey());

        return Response.builder()
                .id(CamundaIds.toUuid(instance.getProcessInstanceKey()))
                .build();
    }
}
