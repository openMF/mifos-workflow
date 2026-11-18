///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.usecase.flowableremote.implementation;

import static org.mifos.workflow.process.core.MifosProcessConstants.MIFOS_WORKFLOW_PROCESS_PROPERTIES_ENGINE;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mifos.workflow.process.core.usecase.MifosProcessSignalUsecase;
import org.mifos.workflow.process.support.flowableremote.core.client.FlowableRemoteClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnProperty(name = MIFOS_WORKFLOW_PROCESS_PROPERTIES_ENGINE, havingValue = "FLOWABLE_REMOTE")
final class FlowableRemoteSignalUsecase implements MifosProcessSignalUsecase {
    private final FlowableRemoteClient client;
    private final FlowableRemoteVariables variables;

    @Override
    public Response execute(Request request) {
        var typed = variables.toTyped(request.getVariables());

        if (request.getId() == null) {
            client.signal(new FlowableRemoteClient.Signal(request.getSignalName(), typed));

            log.debug("broadcast signal {}", request.getSignalName());
        } else {
            var processId = request.getId().toString();
            var executions = FlowableRemotePages.all(
                    (start, size) -> client.executions(processId, request.getSignalName(), start, size));
            if (executions.size() != 1) {
                throw new IllegalArgumentException("Expected one execution of process " + processId
                        + " waiting for signal " + request.getSignalName() + ", found " + executions.size());
            }

            client.executionAction(
                    executions.getFirst().id(),
                    FlowableRemoteClient.ExecutionAction.signalEventReceived(request.getSignalName(), typed));

            log.debug("signalled {} on process {}", request.getSignalName(), request.getId());
        }

        return Response.builder().id(request.getId()).build();
    }
}
