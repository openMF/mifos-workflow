/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifos.workflow.infrastructure.usecase.cadence.implementation;

import static org.mifos.workflow.infrastructure.usecase.cadence.core.CadenceFlowUsecaseConstants.CADENCE_WORKFLOW_PROPERTIES_ENABLED;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mifos.workflow.infrastructure.core.model.MifosFlowTerminateRequest;
import org.mifos.workflow.infrastructure.core.model.MifosFlowTerminateResponse;
import org.mifos.workflow.infrastructure.core.usecase.MifosFlowTerminateUsecase;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnBooleanProperty(CADENCE_WORKFLOW_PROPERTIES_ENABLED)
class CadenceFlowTerminateUsecase implements MifosFlowTerminateUsecase {
    @Override
    public MifosFlowTerminateResponse execute(MifosFlowTerminateRequest request) {
        // TODO: return some sensible data
        return MifosFlowTerminateResponse.builder().build();
    }
}
