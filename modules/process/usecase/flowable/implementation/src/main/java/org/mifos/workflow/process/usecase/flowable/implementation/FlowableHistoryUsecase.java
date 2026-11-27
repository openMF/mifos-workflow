///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.usecase.flowable.implementation;

import static org.mifos.workflow.process.core.MifosProcessConstants.MIFOS_WORKFLOW_PROCESS_PROPERTIES_ENGINE;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.HistoryService;
import org.mifos.workflow.process.core.usecase.MifosProcessHistoryUsecase;
import org.mifos.workflow.process.usecase.flowable.implementation.mapping.FlowableHistoryMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnProperty(name = MIFOS_WORKFLOW_PROCESS_PROPERTIES_ENGINE, havingValue = "FLOWABLE")
final class FlowableHistoryUsecase implements MifosProcessHistoryUsecase {
    private final HistoryService historyService;
    private final FlowableHistoryMapper mapper;

    @Override
    public Response execute(Request request) {
        var query = historyService.createHistoricProcessInstanceQuery().finished();

        if (request.getId() != null) {
            query.processInstanceId(request.getId().toString());
        }

        var history = query.orderByProcessInstanceEndTime().desc().list();

        log.debug("found {} historic process instances", history.size());

        return Response.builder().entries(mapper.map(history)).build();
    }
}
