///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.usecase.cibseven.implementation;

import static org.mifos.workflow.process.core.MifosProcessConstants.MIFOS_WORKFLOW_PROCESS_PROPERTIES_ENGINE;

import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cibseven.bpm.engine.HistoryService;
import org.cibseven.bpm.engine.RuntimeService;
import org.mifos.workflow.process.core.usecase.MifosProcessReplayUsecase;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnProperty(name = MIFOS_WORKFLOW_PROCESS_PROPERTIES_ENGINE, havingValue = "CIBSEVEN")
final class CibsevenReplayUsecase implements MifosProcessReplayUsecase {
    private final RuntimeService runtimeService;
    private final HistoryService historyService;

    @Override
    public Response execute(Request request) {
        var sourceId = request.getSourceProcessId().toString();

        var source = historyService
                .createHistoricProcessInstanceQuery()
                .processInstanceId(sourceId)
                .singleResult();

        var variables = new HashMap<String, Object>();
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

        return Response.builder().id(CibsevenIds.toUuid(replayed.getId())).build();
    }
}
