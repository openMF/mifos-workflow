/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifos.workflow.infrastructure.usecase.flowable.implementation;

import static org.mifos.workflow.infrastructure.usecase.flowable.core.FlowableFlowUsecaseConstants.FLOWABLE_WORKFLOW_PROPERTIES_ENABLED;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RepositoryService;
import org.mifos.workflow.infrastructure.core.model.MifosFlowDeleteRequest;
import org.mifos.workflow.infrastructure.core.model.MifosFlowDeleteResponse;
import org.mifos.workflow.infrastructure.core.usecase.MifosFlowDeleteUsecase;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnBooleanProperty(FLOWABLE_WORKFLOW_PROPERTIES_ENABLED)
class FlowableFlowDeleteUsecase implements MifosFlowDeleteUsecase {
    private final RepositoryService repositoryService;

    @Override
    public MifosFlowDeleteResponse execute(MifosFlowDeleteRequest request) {
        repositoryService.deleteDeployment(request.getDeploymentId(), true);

        // TODO: return some sensible data
        return MifosFlowDeleteResponse.builder().build();
    }
}
