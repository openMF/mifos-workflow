///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.transport.rest.implementation;

import static org.mifos.workflow.process.transport.rest.core.MifosProcessTransportRestConstants.MIFOS_WORKFLOW_PROCESS_TRANSPORT_REST_MIME_TYPE_1_0;
import static org.mifos.workflow.process.transport.rest.core.MifosProcessTransportRestConstants.MIFOS_WORKFLOW_PROCESS_TRANSPORT_REST_ROUTE_REPLAY;
import static org.mifos.workflow.process.transport.rest.core.MifosProcessTransportRestConstants.MIFOS_WORKFLOW_PROCESS_TRANSPORT_REST_TAG;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mifos.workflow.process.core.usecase.MifosProcessReplayUsecase;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(
        value = MIFOS_WORKFLOW_PROCESS_TRANSPORT_REST_ROUTE_REPLAY,
        consumes = MIFOS_WORKFLOW_PROCESS_TRANSPORT_REST_MIME_TYPE_1_0,
        produces = {APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE})
@Tag(name = MIFOS_WORKFLOW_PROCESS_TRANSPORT_REST_TAG)
final class MifosProcessTransportRestReplayController {
    private final MifosProcessReplayUsecase usecase;

    @PostMapping
    MifosProcessReplayUsecase.Response replay(@Valid @RequestBody MifosProcessReplayUsecase.Request request) {
        return usecase.execute(request);
    }
}
