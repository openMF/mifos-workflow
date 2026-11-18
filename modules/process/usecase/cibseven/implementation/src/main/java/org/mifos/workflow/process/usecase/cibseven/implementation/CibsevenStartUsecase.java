///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.usecase.cibseven.implementation;

import static org.mifos.workflow.process.core.MifosProcessConstants.MIFOS_WORKFLOW_PROCESS_PROPERTIES_ENGINE;

import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cibseven.bpm.engine.RuntimeService;
import org.mifos.workflow.process.core.usecase.MifosProcessStartUsecase;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnProperty(name = MIFOS_WORKFLOW_PROCESS_PROPERTIES_ENGINE, havingValue = "CIBSEVEN")
final class CibsevenStartUsecase implements MifosProcessStartUsecase {
    private final RuntimeService runtimeService;

    @Override
    public Response execute(Request request) {
        var instance = runtimeService.startProcessInstanceByKey(
                request.getKey(), Objects.requireNonNullElseGet(request.getVariables(), Map::of));

        log.debug("started process {} with instance id {}", request.getKey(), instance.getId());

        return Response.builder().id(CibsevenIds.toUuid(instance.getId())).build();
    }
}
