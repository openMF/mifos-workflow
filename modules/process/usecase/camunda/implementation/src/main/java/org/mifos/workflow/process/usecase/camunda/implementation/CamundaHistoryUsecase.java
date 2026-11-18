///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.usecase.camunda.implementation;

import static org.mifos.workflow.process.core.MifosProcessConstants.MIFOS_WORKFLOW_PROCESS_PROPERTIES_ENGINE;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.search.response.ProcessInstance;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mifos.workflow.process.core.usecase.MifosProcessHistoryUsecase;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/// Lists finished (completed or cancelled) process instances from Camunda's search API, newest first. Without an id
/// this returns the search API's default page, not the whole history.
@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnProperty(name = MIFOS_WORKFLOW_PROCESS_PROPERTIES_ENGINE, havingValue = "CAMUNDA")
final class CamundaHistoryUsecase implements MifosProcessHistoryUsecase {
    private final CamundaClient client;

    @Override
    public Response execute(Request request) {
        var search = client.newProcessInstanceSearchRequest()
                .sort(sort -> sort.startDate().desc());

        if (request.getId() != null) {
            var key = CamundaIds.toKey(request.getId());
            search = search.filter(filter -> filter.processInstanceKey(key));
        }

        var entries = search.send().join().items().stream()
                .filter(instance -> instance.getEndDate() != null)
                .map(CamundaHistoryUsecase::map)
                .toList();

        log.debug("found {} history entries", entries.size());

        return Response.builder().entries(entries).build();
    }

    private static MifosFlowHistoryEntry map(ProcessInstance instance) {
        var start = instance.getStartDate();
        var end = instance.getEndDate();

        return MifosFlowHistoryEntry.builder()
                .processId(CamundaIds.toId(instance.getProcessInstanceKey()))
                .processDefinitionId(CamundaIds.toId(instance.getProcessDefinitionKey()))
                .processDefinitionKey(instance.getProcessDefinitionId())
                .startTime(CamundaTaskPendingUsecase.toLocalDateTime(start))
                .endTime(CamundaTaskPendingUsecase.toLocalDateTime(end))
                .durationInMillis(
                        start == null || end == null
                                ? null
                                : Duration.between(start, end).toMillis())
                .build();
    }
}
