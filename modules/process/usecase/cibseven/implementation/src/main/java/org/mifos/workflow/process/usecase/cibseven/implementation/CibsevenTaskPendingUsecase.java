///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.usecase.cibseven.implementation;

import static org.mifos.workflow.process.core.MifosProcessConstants.MIFOS_WORKFLOW_PROCESS_PROPERTIES_ENGINE;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cibseven.bpm.engine.TaskService;
import org.mifos.workflow.process.core.usecase.MifosProcessTaskPendingUsecase;
import org.mifos.workflow.process.usecase.cibseven.implementation.mapping.CibsevenTaskPendingMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnProperty(name = MIFOS_WORKFLOW_PROCESS_PROPERTIES_ENGINE, havingValue = "CIBSEVEN")
final class CibsevenTaskPendingUsecase implements MifosProcessTaskPendingUsecase {
    private final TaskService taskService;
    private final CibsevenTaskPendingMapper mapper;

    @Override
    public Response execute(Request request) {
        var tasks =
                taskService.createTaskQuery().taskAssignee(request.getUserId()).list();

        log.debug("found {} pending tasks for {}", tasks.size(), request.getUserId());

        return Response.builder().tasks(mapper.map(tasks)).build();
    }
}
