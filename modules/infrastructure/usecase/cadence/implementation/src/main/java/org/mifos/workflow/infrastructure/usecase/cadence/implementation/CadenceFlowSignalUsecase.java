/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifos.workflow.infrastructure.usecase.cadence.implementation;

import static org.mifos.workflow.infrastructure.usecase.cadence.core.CadenceFlowUsecaseConstants.CADENCE_WORKFLOW_PROPERTIES_ENABLED;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mifos.workflow.infrastructure.core.model.MifosFlowSignalRequest;
import org.mifos.workflow.infrastructure.core.model.MifosFlowSignalResponse;
import org.mifos.workflow.infrastructure.core.usecase.MifosFlowSignalUsecase;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnBooleanProperty(CADENCE_WORKFLOW_PROPERTIES_ENABLED)
class CadenceFlowSignalUsecase implements MifosFlowSignalUsecase {
    @Override
    public MifosFlowSignalResponse execute(MifosFlowSignalRequest request) {
        // TODO: return some sensible data
        return MifosFlowSignalResponse.builder().build();
    }
}
