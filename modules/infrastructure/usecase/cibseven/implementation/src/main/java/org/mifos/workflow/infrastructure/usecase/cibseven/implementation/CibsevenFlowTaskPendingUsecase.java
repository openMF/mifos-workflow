/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifos.workflow.infrastructure.usecase.cibseven.implementation;

import static org.mifos.workflow.infrastructure.usecase.cibseven.core.CibsevenFlowUsecaseConstants.CIBSEVEN_WORKFLOW_PROPERTIES_ENABLED;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cibseven.bpm.engine.TaskService;
import org.mifos.workflow.infrastructure.core.model.MifosFlowTaskPendingRequest;
import org.mifos.workflow.infrastructure.core.model.MifosFlowTaskPendingResponse;
import org.mifos.workflow.infrastructure.core.usecase.MifosFlowTaskPendingUsecase;
import org.mifos.workflow.infrastructure.usecase.cibseven.mapping.CibsevenTaskPendingMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnBooleanProperty(CIBSEVEN_WORKFLOW_PROPERTIES_ENABLED)
class CibsevenFlowTaskPendingUsecase implements MifosFlowTaskPendingUsecase {
    private final TaskService taskService;
    private final CibsevenTaskPendingMapper mapper;
    @Override
    public MifosFlowTaskPendingResponse execute(MifosFlowTaskPendingRequest request) {
        var tasks = taskService.createTaskQuery()
                .taskAssignee(request.getUserId())
                .list();

        log.debug("found {} pending tasks for {}", tasks.size(), request.getUserId());

        return MifosFlowTaskPendingResponse.builder()
                .tasks(mapper.map(tasks))
                .build();
    }
}
