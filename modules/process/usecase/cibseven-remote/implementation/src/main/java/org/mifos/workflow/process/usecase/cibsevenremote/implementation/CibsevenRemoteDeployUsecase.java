///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.usecase.cibsevenremote.implementation;

import static org.mifos.workflow.process.core.MifosProcessConstants.MIFOS_WORKFLOW_PROCESS_PROPERTIES_ENGINE;

import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mifos.workflow.process.core.usecase.MifosProcessDeployUsecase;
import org.mifos.workflow.process.support.cibsevenremote.core.client.CibsevenRemoteClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnProperty(name = MIFOS_WORKFLOW_PROCESS_PROPERTIES_ENGINE, havingValue = "CIBSEVEN_REMOTE")
final class CibsevenRemoteDeployUsecase implements MifosProcessDeployUsecase {
    private final CibsevenRemoteClient client;

    @Override
    public Response execute(Request request) {
        var parts = new MultipartBodyBuilder();
        parts.part("deployment-name", request.getName());
        parts.part("data", request.getProcessDefinition().getBytes(StandardCharsets.UTF_8))
                .filename(resourceName(request.getName()))
                .contentType(MediaType.APPLICATION_XML);

        var deployment = client.createDeployment(parts.build());

        log.debug("deployed process definition {} with id {}", request.getName(), deployment.id());

        return Response.builder().id(CibsevenIds.toUuid(deployment.id())).build();
    }

    /// The engine only parses resources whose names end in a BPMN suffix; anything else is stored but silently
    /// ignored, producing a deployment without process definitions.
    private static String resourceName(String name) {
        return name.endsWith(".bpmn") || name.endsWith(".bpmn20.xml") ? name : name + ".bpmn";
    }
}
