/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifos.workflow.infrastructure.usecase.cibseven.implementation;

import static org.mifos.workflow.infrastructure.usecase.cibseven.core.CibsevenFlowUsecaseConstants.CIBSEVEN_WORKFLOW_PROPERTIES_ENABLED;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cibseven.bpm.engine.HistoryService;
import org.cibseven.bpm.engine.RuntimeService;
import org.mifos.workflow.infrastructure.core.model.MifosFlowReplayRequest;
import org.mifos.workflow.infrastructure.core.model.MifosFlowReplayResponse;
import org.mifos.workflow.infrastructure.core.usecase.MifosFlowReplayUsecase;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnBooleanProperty(CIBSEVEN_WORKFLOW_PROPERTIES_ENABLED)
class CibsevenFlowReplayUsecase implements MifosFlowReplayUsecase {
    private final RuntimeService runtimeService;
    private final HistoryService historyService;
    @Override
    public MifosFlowReplayResponse execute(MifosFlowReplayRequest request) {
        var sourceId = request.getSourceProcessId().toString();

        var source = historyService
                .createHistoricProcessInstanceQuery()
                .processInstanceId(sourceId)
                .singleResult();

        Map<String, Object> variables = new HashMap<>();
        historyService
                .createHistoricVariableInstanceQuery()
                .processInstanceId(sourceId)
                .list()
                .forEach(v -> variables.put(v.getName(), v.getValue()));

        if (request.getVariables() != null) {
            variables.putAll(request.getVariables());
        }

        var replayed = runtimeService.startProcessInstanceByKey(source.getProcessDefinitionKey(), variables);

        log.debug("replayed process {} as {}", sourceId, replayed.getId());

        return MifosFlowReplayResponse.builder()
                .id(CibsevenIds.toUuid(replayed.getId()))
                .build();
    }
}
