/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifos.workflow.infrastructure.usecase.eximee.implementation;

import static org.mifos.workflow.infrastructure.usecase.eximee.core.EximeeFlowUsecaseConstants.EXIMEE_WORKFLOW_PROPERTIES_ENABLED;

import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eximeebpms.bpm.engine.RuntimeService;
import org.mifos.workflow.infrastructure.core.model.MifosFlowStartRequest;
import org.mifos.workflow.infrastructure.core.model.MifosFlowStartResponse;
import org.mifos.workflow.infrastructure.core.usecase.MifosFlowStartUsecase;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnBooleanProperty(EXIMEE_WORKFLOW_PROPERTIES_ENABLED)
class EximeeFlowStartUsecase implements MifosFlowStartUsecase {
    private final RuntimeService runtimeService;

    @Override
    public MifosFlowStartResponse execute(MifosFlowStartRequest request) {
        var instance = runtimeService.startProcessInstanceByKey(
                request.getKey(), Objects.requireNonNullElseGet(request.getVariables(), Map::of));

        log.debug("started process {} with instance id {}", request.getKey(), instance.getId());

        return MifosFlowStartResponse.builder()
                .id(EximeeIds.toUuid(instance.getId()))
                .build();
    }
}