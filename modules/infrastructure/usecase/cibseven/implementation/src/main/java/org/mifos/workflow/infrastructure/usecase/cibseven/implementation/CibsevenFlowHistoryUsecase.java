/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifos.workflow.infrastructure.usecase.cibseven.implementation;

import static org.mifos.workflow.infrastructure.usecase.cibseven.core.CibsevenFlowUsecaseConstants.CIBSEVEN_WORKFLOW_PROPERTIES_ENABLED;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cibseven.bpm.engine.HistoryService;
import org.mifos.workflow.infrastructure.core.model.MifosFlowHistoryRequest;
import org.mifos.workflow.infrastructure.core.model.MifosFlowHistoryResponse;
import org.mifos.workflow.infrastructure.core.usecase.MifosFlowHistoryUsecase;
import org.mifos.workflow.infrastructure.usecase.cibseven.mapping.CibsevenHistoryMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnBooleanProperty(CIBSEVEN_WORKFLOW_PROPERTIES_ENABLED)
class CibsevenFlowHistoryUsecase implements MifosFlowHistoryUsecase {
    private final HistoryService historyService;
    private final CibsevenHistoryMapper mapper;
    @Override
    public MifosFlowHistoryResponse execute(MifosFlowHistoryRequest request) {
        var query = historyService.createHistoricProcessInstanceQuery().finished();

        if (request.getId() != null) {
            query = query.processInstanceId(request.getId().toString());
        }

        var instances = query.list();

        log.debug("found {} history entries", instances.size());

        return MifosFlowHistoryResponse.builder()
                .entries(mapper.map(instances))
                .build();
    }
}
