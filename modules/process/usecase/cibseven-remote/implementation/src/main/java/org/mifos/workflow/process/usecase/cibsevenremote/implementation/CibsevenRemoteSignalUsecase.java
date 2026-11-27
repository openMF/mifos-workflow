///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.usecase.cibsevenremote.implementation;

import static org.mifos.workflow.process.core.MifosProcessConstants.MIFOS_WORKFLOW_PROCESS_PROPERTIES_ENGINE;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mifos.workflow.process.core.usecase.MifosProcessSignalUsecase;
import org.mifos.workflow.process.support.cibsevenremote.core.client.CibsevenRemoteClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnProperty(name = MIFOS_WORKFLOW_PROCESS_PROPERTIES_ENGINE, havingValue = "CIBSEVEN_REMOTE")
final class CibsevenRemoteSignalUsecase implements MifosProcessSignalUsecase {
    private final CibsevenRemoteClient client;
    private final CibsevenRemoteVariables variables;

    @Override
    public Response execute(Request request) {
        var typed = variables.toTyped(request.getVariables());

        if (request.getId() == null) {
            client.signal(new CibsevenRemoteClient.Signal(request.getSignalName(), null, typed));

            log.debug("broadcast signal {}", request.getSignalName());
        } else {
            var processId = request.getId().toString();
            var executions = client.executions(processId, request.getSignalName());
            if (executions.size() != 1) {
                throw new IllegalArgumentException("Expected one execution of process " + processId
                        + " waiting for signal " + request.getSignalName() + ", found " + executions.size());
            }

            client.signal(new CibsevenRemoteClient.Signal(
                    request.getSignalName(), executions.getFirst().id(), typed));

            log.debug("signalled {} on process {}", request.getSignalName(), request.getId());
        }

        return Response.builder().id(request.getId()).build();
    }
}
