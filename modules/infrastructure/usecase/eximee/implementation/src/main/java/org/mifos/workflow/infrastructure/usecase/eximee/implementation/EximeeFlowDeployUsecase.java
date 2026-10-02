/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifos.workflow.infrastructure.usecase.eximee.implementation;

import static org.mifos.workflow.infrastructure.usecase.eximee.core.EximeeFlowUsecaseConstants.EXIMEE_WORKFLOW_PROPERTIES_ENABLED;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eximeebpms.bpm.engine.RepositoryService;
import org.mifos.workflow.infrastructure.core.model.MifosFlowDeployRequest;
import org.mifos.workflow.infrastructure.core.model.MifosFlowDeployResponse;
import org.mifos.workflow.infrastructure.core.usecase.MifosFlowDeployUsecase;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnBooleanProperty(EXIMEE_WORKFLOW_PROPERTIES_ENABLED)
class EximeeFlowDeployUsecase implements MifosFlowDeployUsecase {
    private final RepositoryService repositoryService;
    @Override
    public MifosFlowDeployResponse execute(MifosFlowDeployRequest request) {
        var deployment = repositoryService.createDeployment()
                .addInputStream(
                        request.getName(),
                        new ByteArrayInputStream(request.getProcessDefinition().getBytes(StandardCharsets.UTF_8)))
                .name(request.getName())
                .deploy();

        log.debug("deployed process definition {} with id {}", request.getName(), deployment.getId());

        return MifosFlowDeployResponse.builder()
                .id(EximeeIds.toUuid(deployment.getId()))
                .build();
    }
}
