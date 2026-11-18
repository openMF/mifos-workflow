///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.usecase.flowable.implementation;

import static org.mifos.workflow.process.core.MifosProcessConstants.MIFOS_WORKFLOW_PROCESS_PROPERTIES_ENGINE;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RepositoryService;
import org.mifos.workflow.process.core.usecase.MifosProcessDeleteUsecase;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnProperty(name = MIFOS_WORKFLOW_PROCESS_PROPERTIES_ENGINE, havingValue = "FLOWABLE")
final class FlowableDeleteUsecase implements MifosProcessDeleteUsecase {
    private final RepositoryService repositoryService;

    @Override
    public Response execute(Request request) {
        repositoryService.deleteDeployment(request.getDeploymentId(), true);

        log.debug("deleted deployment {}", request.getDeploymentId());

        return Response.builder().id(UUID.fromString(request.getDeploymentId())).build();
    }
}
