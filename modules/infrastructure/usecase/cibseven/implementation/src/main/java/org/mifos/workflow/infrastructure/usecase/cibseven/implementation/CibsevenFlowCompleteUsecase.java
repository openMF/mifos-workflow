/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifos.workflow.infrastructure.usecase.cibseven.implementation;

import static org.mifos.workflow.infrastructure.usecase.cibseven.core.CibsevenFlowUsecaseConstants.CIBSEVEN_WORKFLOW_PROPERTIES_ENABLED;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mifos.workflow.infrastructure.core.model.MifosFlowCompleteRequest;
import org.mifos.workflow.infrastructure.core.model.MifosFlowCompleteResponse;
import org.mifos.workflow.infrastructure.core.usecase.MifosFlowCompleteUsecase;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnBooleanProperty(CIBSEVEN_WORKFLOW_PROPERTIES_ENABLED)
class CibsevenFlowCompleteUsecase implements MifosFlowCompleteUsecase {
    @Override
    public MifosFlowCompleteResponse execute(MifosFlowCompleteRequest request) {
        // TODO: return some sensible data
        return MifosFlowCompleteResponse.builder().build();
    }
}
