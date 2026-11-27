///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.usecase.operaton.implementation;

import static org.mifos.workflow.process.core.MifosProcessConstants.MIFOS_WORKFLOW_PROCESS_PROPERTIES_ENGINE;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mifos.workflow.process.core.usecase.MifosProcessDeployUsecase;
import org.operaton.bpm.engine.RepositoryService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnProperty(name = MIFOS_WORKFLOW_PROCESS_PROPERTIES_ENGINE, havingValue = "OPERATON")
final class OperatonDeployUsecase implements MifosProcessDeployUsecase {
    private final RepositoryService repositoryService;

    @Override
    public Response execute(Request request) {
        var deployment = repositoryService
                .createDeployment()
                .addInputStream(
                        resourceName(request.getName()),
                        new ByteArrayInputStream(request.getProcessDefinition().getBytes(StandardCharsets.UTF_8)))
                .name(request.getName())
                .deploy();

        log.debug("deployed process definition {} with id {}", request.getName(), deployment.getId());

        return Response.builder().id(OperatonIds.toUuid(deployment.getId())).build();
    }

    /// The engine only parses resources whose names end in a BPMN suffix; anything else is stored but silently
    /// ignored, producing a deployment without process definitions.
    private static String resourceName(String name) {
        return name.endsWith(".bpmn") || name.endsWith(".bpmn20.xml") ? name : name + ".bpmn";
    }
}
