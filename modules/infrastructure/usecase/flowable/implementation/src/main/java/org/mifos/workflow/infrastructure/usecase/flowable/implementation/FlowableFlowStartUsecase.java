/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifos.workflow.infrastructure.usecase.flowable.implementation;

import static org.mifos.workflow.infrastructure.usecase.flowable.core.FlowableFlowUsecaseConstants.FLOWABLE_WORKFLOW_PROPERTIES_ENABLED;

import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RuntimeService;
import org.mifos.workflow.infrastructure.core.model.MifosFlowStartRequest;
import org.mifos.workflow.infrastructure.core.model.MifosFlowStartResponse;
import org.mifos.workflow.infrastructure.core.usecase.MifosFlowStartUsecase;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnBooleanProperty(FLOWABLE_WORKFLOW_PROPERTIES_ENABLED)
class FlowableFlowStartUsecase implements MifosFlowStartUsecase {
    private final RuntimeService runtimeService;

    @Override
    public MifosFlowStartResponse execute(MifosFlowStartRequest request) {
        // var flowableInstance =
        runtimeService.startProcessInstanceByKey(
                request.getKey(), Objects.requireNonNullElseGet(request.getVariables(), Map::of));

        // TODO: return some sensible data
        return MifosFlowStartResponse.builder().build();
    }
}
