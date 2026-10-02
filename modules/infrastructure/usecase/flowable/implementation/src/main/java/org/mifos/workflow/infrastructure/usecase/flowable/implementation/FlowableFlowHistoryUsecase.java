/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifos.workflow.infrastructure.usecase.flowable.implementation;

import static org.mifos.workflow.infrastructure.usecase.flowable.core.FlowableFlowUsecaseConstants.FLOWABLE_WORKFLOW_PROPERTIES_ENABLED;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.HistoryService;
import org.mifos.workflow.infrastructure.core.model.MifosFlowHistoryRequest;
import org.mifos.workflow.infrastructure.core.model.MifosFlowHistoryResponse;
import org.mifos.workflow.infrastructure.core.usecase.MifosFlowHistoryUsecase;
import org.mifos.workflow.infrastructure.usecase.flowable.mapping.FlowableHistoryMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnBooleanProperty(FLOWABLE_WORKFLOW_PROPERTIES_ENABLED)
class FlowableFlowHistoryUsecase implements MifosFlowHistoryUsecase {
    private final HistoryService historyService;
    private final FlowableHistoryMapper mapper;

    @Override
    public MifosFlowHistoryResponse execute(MifosFlowHistoryRequest request) {
        var query = historyService.createHistoricProcessInstanceQuery().finished();

        if (request.getId() != null) {
            query = query.processInstanceId(request.getId().toString());
        }

        var history = query.orderByProcessInstanceEndTime().desc().list();

        log.debug("found {} historic process instances", history.size());

        return MifosFlowHistoryResponse.builder()
                .entries(mapper.map(history))
                .build();
    }
}
