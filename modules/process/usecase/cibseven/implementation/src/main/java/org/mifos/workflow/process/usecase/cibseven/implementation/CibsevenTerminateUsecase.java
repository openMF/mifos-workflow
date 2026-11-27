///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.usecase.cibseven.implementation;

import static org.mifos.workflow.process.core.MifosProcessConstants.MIFOS_WORKFLOW_PROCESS_PROPERTIES_ENGINE;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cibseven.bpm.engine.RuntimeService;
import org.mifos.workflow.process.core.usecase.MifosProcessTerminateUsecase;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnProperty(name = MIFOS_WORKFLOW_PROCESS_PROPERTIES_ENGINE, havingValue = "CIBSEVEN")
final class CibsevenTerminateUsecase implements MifosProcessTerminateUsecase {
    private final RuntimeService runtimeService;

    @Override
    public Response execute(Request request) {
        runtimeService.deleteProcessInstance(request.getProcessId(), request.getReason());

        log.debug("terminated process {}", request.getProcessId());

        return Response.builder().id(CibsevenIds.toUuid(request.getProcessId())).build();
    }
}
