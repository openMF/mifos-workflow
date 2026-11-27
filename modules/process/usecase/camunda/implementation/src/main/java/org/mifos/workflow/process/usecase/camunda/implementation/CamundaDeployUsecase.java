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
import org.mifos.workflow.process.core.usecase.MifosProcessDeployUsecase;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/// Deploys one BPMN resource. Camunda 8 cannot delete deployments, only the resources in them, so the id returned is
/// the key of the deployed process definition, which `CamundaDeleteUsecase` accepts.
@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnProperty(name = MIFOS_WORKFLOW_PROCESS_PROPERTIES_ENGINE, havingValue = "CAMUNDA")
final class CamundaDeployUsecase implements MifosProcessDeployUsecase {
    private final CamundaClient client;

    @Override
    public Response execute(Request request) {
        var deployment = client.newDeployResourceCommand()
                .addResourceStringUtf8(request.getProcessDefinition(), resourceName(request.getName()))
                .send()
                .join();

        var key = deployment.getProcesses().isEmpty()
                ? deployment.getKey()
                : deployment.getProcesses().getFirst().getProcessDefinitionKey();

        log.debug("deployed process definition {} with key {}", request.getName(), key);

        return Response.builder().id(CamundaIds.toUuid(key)).build();
    }

    /// The broker picks the parser by file extension, so a name without a BPMN suffix would not deploy as a process.
    private static String resourceName(String name) {
        return name.endsWith(".bpmn") || name.endsWith(".bpmn20.xml") ? name : name + ".bpmn";
    }
}
