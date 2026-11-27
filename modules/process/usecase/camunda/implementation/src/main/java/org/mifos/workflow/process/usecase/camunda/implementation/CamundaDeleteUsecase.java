///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.usecase.camunda.implementation;

import static org.mifos.workflow.process.core.MifosProcessConstants.MIFOS_WORKFLOW_PROCESS_PROPERTIES_ENGINE;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.command.ClientException;
import io.camunda.client.api.search.enums.ProcessInstanceState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mifos.workflow.process.core.usecase.MifosProcessDeleteUsecase;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/// Deletes the process definition whose key `CamundaDeployUsecase` returned, together with its running instances,
/// like the other engines' cascading deployment delete.
///
/// Camunda 8 leaves running instances alone when their definition is deleted, so they are cancelled first. They are
/// found through the search API, which trails the broker slightly and returns one page: an instance started an
/// instant before the delete, or beyond the first page, keeps running.
@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnProperty(name = MIFOS_WORKFLOW_PROCESS_PROPERTIES_ENGINE, havingValue = "CAMUNDA")
final class CamundaDeleteUsecase implements MifosProcessDeleteUsecase {
    private final CamundaClient client;

    @Override
    public Response execute(Request request) {
        var key = CamundaIds.toKey(request.getDeploymentId());

        cancelActiveInstances(key);
        client.newDeleteResourceCommand(key).send().join();

        log.debug("deleted resource {}", key);

        return Response.builder().id(CamundaIds.toUuid(key)).build();
    }

    private void cancelActiveInstances(long processDefinitionKey) {
        var instances = client.newProcessInstanceSearchRequest()
                .filter(filter ->
                        filter.processDefinitionKey(processDefinitionKey).state(ProcessInstanceState.ACTIVE))
                .send()
                .join()
                .items();

        for (var instance : instances) {
            try {
                client.newCancelInstanceCommand(instance.getProcessInstanceKey())
                        .send()
                        .join();
            } catch (ClientException e) {
                // most likely finished between the search and the cancel; nothing left to cancel
                log.warn("could not cancel process {}", instance.getProcessInstanceKey(), e);
            }
        }

        log.debug("cancelled {} running instances of {}", instances.size(), processDefinitionKey);
    }
}
