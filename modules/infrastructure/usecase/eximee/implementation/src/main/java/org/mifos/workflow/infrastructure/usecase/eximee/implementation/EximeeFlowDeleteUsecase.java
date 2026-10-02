/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifos.workflow.infrastructure.usecase.eximee.implementation;

import static org.mifos.workflow.infrastructure.usecase.eximee.core.EximeeFlowUsecaseConstants.EXIMEE_WORKFLOW_PROPERTIES_ENABLED;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eximeebpms.bpm.engine.RepositoryService;
import org.mifos.workflow.infrastructure.core.model.MifosFlowDeleteRequest;
import org.mifos.workflow.infrastructure.core.model.MifosFlowDeleteResponse;
import org.mifos.workflow.infrastructure.core.usecase.MifosFlowDeleteUsecase;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnBooleanProperty(EXIMEE_WORKFLOW_PROPERTIES_ENABLED)
class EximeeFlowDeleteUsecase implements MifosFlowDeleteUsecase {
    private final RepositoryService repositoryService;
    @Override
    public MifosFlowDeleteResponse execute(MifosFlowDeleteRequest request) {
        repositoryService.deleteDeployment(request.getDeploymentId().toString(), true);

        log.debug("deleted deployment {}", request.getDeploymentId());

        return MifosFlowDeleteResponse.builder().id(EximeeIds.toUuid(request.getDeploymentId())).build();
    }
}
