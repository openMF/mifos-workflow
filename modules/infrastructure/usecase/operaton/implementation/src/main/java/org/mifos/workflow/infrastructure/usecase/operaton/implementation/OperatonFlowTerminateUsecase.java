/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifos.workflow.infrastructure.usecase.operaton.implementation;

import static org.mifos.workflow.infrastructure.usecase.operaton.core.OperatonFlowUsecaseConstants.OPERATON_WORKFLOW_PROPERTIES_ENABLED;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.operaton.bpm.engine.RuntimeService;
import org.mifos.workflow.infrastructure.core.model.MifosFlowTerminateRequest;
import org.mifos.workflow.infrastructure.core.model.MifosFlowTerminateResponse;
import org.mifos.workflow.infrastructure.core.usecase.MifosFlowTerminateUsecase;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnBooleanProperty(OPERATON_WORKFLOW_PROPERTIES_ENABLED)
class OperatonFlowTerminateUsecase implements MifosFlowTerminateUsecase {
    private final RuntimeService runtimeService;
    @Override
    public MifosFlowTerminateResponse execute(MifosFlowTerminateRequest request) {
        runtimeService.deleteProcessInstance(request.getProcessId().toString(), request.getReason());

        log.debug("terminated process {}", request.getProcessId());

        return MifosFlowTerminateResponse.builder().id(OperatonIds.toUuid(request.getProcessId())).build();
    }
}
