/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifos.workflow.infrastructure.usecase.flowable.implementation;

import static org.mifos.workflow.infrastructure.usecase.flowable.core.FlowableFlowUsecaseConstants.FLOWABLE_WORKFLOW_PROPERTIES_ENABLED;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.TaskService;
import org.mifos.workflow.infrastructure.core.model.MifosFlowTaskPendingRequest;
import org.mifos.workflow.infrastructure.core.model.MifosFlowTaskPendingResponse;
import org.mifos.workflow.infrastructure.core.usecase.MifosFlowTaskPendingUsecase;
import org.mifos.workflow.infrastructure.usecase.flowable.mapping.FlowableTaskPendingMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnBooleanProperty(FLOWABLE_WORKFLOW_PROPERTIES_ENABLED)
class FlowableFlowTaskPendingUsecase implements MifosFlowTaskPendingUsecase {
    private final TaskService taskService;
    private final FlowableTaskPendingMapper mapper;

    @Override
    public MifosFlowTaskPendingResponse execute(MifosFlowTaskPendingRequest request) {
        // var tasks =
        taskService.createTaskQuery().taskAssignee(request.getUserId()).list();

        // TODO: return some sensible data
        return MifosFlowTaskPendingResponse.builder().build();
    }
}
