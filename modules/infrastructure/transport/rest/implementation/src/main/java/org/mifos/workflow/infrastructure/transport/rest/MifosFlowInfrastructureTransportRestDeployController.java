/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifos.workflow.infrastructure.transport.rest;

import static org.mifos.workflow.infrastructure.transport.rest.core.MifosFlowInfrastructureTransportRestConstants.MIFOS_WORKFLOW_INFRASTRUCTURE_TRANSPORT_REST_MIME_TYPE_1_0;
import static org.mifos.workflow.infrastructure.transport.rest.core.MifosFlowInfrastructureTransportRestConstants.MIFOS_WORKFLOW_INFRASTRUCTURE_TRANSPORT_REST_ROUTE_DEPLOY;
import static org.mifos.workflow.infrastructure.transport.rest.core.MifosFlowInfrastructureTransportRestConstants.MIFOS_WORKFLOW_INFRASTRUCTURE_TRANSPORT_REST_TAG;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mifos.workflow.infrastructure.core.model.MifosFlowDeployRequest;
import org.mifos.workflow.infrastructure.core.model.MifosFlowDeployResponse;
import org.mifos.workflow.infrastructure.core.usecase.MifosFlowDeployUsecase;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(
        value = MIFOS_WORKFLOW_INFRASTRUCTURE_TRANSPORT_REST_ROUTE_DEPLOY,
        consumes = MIFOS_WORKFLOW_INFRASTRUCTURE_TRANSPORT_REST_MIME_TYPE_1_0,
        produces = {APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE})
@Tag(name = MIFOS_WORKFLOW_INFRASTRUCTURE_TRANSPORT_REST_TAG)
class MifosFlowInfrastructureTransportRestDeployController {
    private final MifosFlowDeployUsecase usecase;

    @PostMapping
    MifosFlowDeployResponse deploy(@Valid @RequestBody MifosFlowDeployRequest request) {
        return usecase.execute(request);
    }
}
