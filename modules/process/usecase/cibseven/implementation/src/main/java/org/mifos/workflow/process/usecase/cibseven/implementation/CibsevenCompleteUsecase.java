///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.usecase.cibseven.implementation;

import static org.mifos.workflow.process.core.MifosProcessConstants.MIFOS_WORKFLOW_PROCESS_PROPERTIES_ENGINE;

import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cibseven.bpm.engine.TaskService;
import org.mifos.workflow.process.core.usecase.MifosProcessCompleteUsecase;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnProperty(name = MIFOS_WORKFLOW_PROCESS_PROPERTIES_ENGINE, havingValue = "CIBSEVEN")
final class CibsevenCompleteUsecase implements MifosProcessCompleteUsecase {
    private final TaskService taskService;

    @Override
    public Response execute(Request request) {
        taskService.complete(request.getTaskId(), Objects.requireNonNullElseGet(request.getVariables(), Map::of));

        log.debug("completed task {}", request.getTaskId());

        return Response.builder().id(CibsevenIds.toUuid(request.getTaskId())).build();
    }
}
