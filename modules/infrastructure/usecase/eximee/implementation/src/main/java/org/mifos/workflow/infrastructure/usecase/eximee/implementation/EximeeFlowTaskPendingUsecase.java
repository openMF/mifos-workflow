/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifos.workflow.infrastructure.usecase.eximee.implementation;

import static org.mifos.workflow.infrastructure.usecase.eximee.core.EximeeFlowUsecaseConstants.EXIMEE_WORKFLOW_PROPERTIES_ENABLED;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eximeebpms.bpm.engine.TaskService;
import org.mifos.workflow.infrastructure.core.model.MifosFlowTaskPendingRequest;
import org.mifos.workflow.infrastructure.core.model.MifosFlowTaskPendingResponse;
import org.mifos.workflow.infrastructure.core.usecase.MifosFlowTaskPendingUsecase;
import org.mifos.workflow.infrastructure.usecase.eximee.mapping.EximeeTaskPendingMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnBooleanProperty(EXIMEE_WORKFLOW_PROPERTIES_ENABLED)
class EximeeFlowTaskPendingUsecase implements MifosFlowTaskPendingUsecase {
    private final TaskService taskService;
    private final EximeeTaskPendingMapper mapper;
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
