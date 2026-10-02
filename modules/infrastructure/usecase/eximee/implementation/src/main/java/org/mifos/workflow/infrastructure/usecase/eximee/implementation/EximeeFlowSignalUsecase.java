/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifos.workflow.infrastructure.usecase.eximee.implementation;

import static org.mifos.workflow.infrastructure.usecase.eximee.core.EximeeFlowUsecaseConstants.EXIMEE_WORKFLOW_PROPERTIES_ENABLED;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eximeebpms.bpm.engine.RuntimeService;
import org.mifos.workflow.infrastructure.core.model.MifosFlowSignalRequest;
import org.mifos.workflow.infrastructure.core.model.MifosFlowSignalResponse;
import org.mifos.workflow.infrastructure.core.usecase.MifosFlowSignalUsecase;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnBooleanProperty(EXIMEE_WORKFLOW_PROPERTIES_ENABLED)
class EximeeFlowSignalUsecase implements MifosFlowSignalUsecase {
    private final RuntimeService runtimeService;
    @Override
    public MifosFlowSignalResponse execute(MifosFlowSignalRequest request) {
        Map<String, Object> variables = Objects.requireNonNullElseGet(request.getVariables(), Map::of);

        if (request.getId() == null) {
            runtimeService.signalEventReceived(request.getSignalName(), variables);
            log.debug("broadcast signal {}", request.getSignalName());
        } else {
            var execution = runtimeService
                    .createExecutionQuery()
                    .processInstanceId(request.getId().toString())
                    .signalEventSubscriptionName(request.getSignalName())
                    .singleResult();

            runtimeService.signalEventReceived(request.getSignalName(), execution.getId(), variables);
            log.debug("signalled {} on process {}", request.getSignalName(), request.getId());
        }

        return MifosFlowSignalResponse.builder().id(request.getId()).build();
    }
}
