///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.usecase.operaton.implementation;

import static org.mifos.workflow.process.core.MifosProcessConstants.MIFOS_WORKFLOW_PROCESS_PROPERTIES_ENGINE;

import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mifos.workflow.process.core.usecase.MifosProcessSignalUsecase;
import org.operaton.bpm.engine.RuntimeService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnProperty(name = MIFOS_WORKFLOW_PROCESS_PROPERTIES_ENGINE, havingValue = "OPERATON")
final class OperatonSignalUsecase implements MifosProcessSignalUsecase {
    private final RuntimeService runtimeService;

    @Override
    public Response execute(Request request) {
        var variables = Objects.requireNonNullElseGet(request.getVariables(), Map::<String, Object>of);

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

        return Response.builder().id(request.getId()).build();
    }
}
