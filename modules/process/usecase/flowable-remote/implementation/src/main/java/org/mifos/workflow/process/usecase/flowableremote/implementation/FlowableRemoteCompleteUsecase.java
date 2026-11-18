///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.usecase.flowableremote.implementation;

import static org.mifos.workflow.process.core.MifosProcessConstants.MIFOS_WORKFLOW_PROCESS_PROPERTIES_ENGINE;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mifos.workflow.process.core.usecase.MifosProcessCompleteUsecase;
import org.mifos.workflow.process.support.flowableremote.core.client.FlowableRemoteClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnProperty(name = MIFOS_WORKFLOW_PROCESS_PROPERTIES_ENGINE, havingValue = "FLOWABLE_REMOTE")
final class FlowableRemoteCompleteUsecase implements MifosProcessCompleteUsecase {
    private final FlowableRemoteClient client;
    private final FlowableRemoteVariables variables;

    @Override
    public Response execute(Request request) {
        client.taskAction(
                request.getTaskId(),
                FlowableRemoteClient.TaskAction.complete(variables.toTyped(request.getVariables())));

        log.debug("completed task {}", request.getTaskId());

        return Response.builder().id(FlowableIds.toUuid(request.getTaskId())).build();
    }
}
