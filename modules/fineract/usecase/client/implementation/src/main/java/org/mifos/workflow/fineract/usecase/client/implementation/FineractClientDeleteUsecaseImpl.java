/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifos.workflow.fineract.usecase.client.implementation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mifos.commons.oas.imperative.sdk.fineract.implementation.ClientApi;
import org.mifos.workflow.fineract.usecase.client.core.model.FineractClientDeleteRequest;
import org.mifos.workflow.fineract.usecase.client.core.model.FineractClientDeleteResponse;
import org.mifos.workflow.fineract.usecase.client.core.usecase.FineractClientDeleteUsecase;
import org.mifos.workflow.fineract.usecase.client.mapping.FineractClientRequestMapper;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
final class FineractClientDeleteUsecaseImpl implements FineractClientDeleteUsecase {
    private final ClientApi clientApi;
    private final FineractClientRequestMapper requestMapper;

    @Override
    public FineractClientDeleteResponse execute(FineractClientDeleteRequest request) {
        var response = clientApi.deleteClient(request.getClientId()).getBody();
        return requestMapper.map(response);
    }
}
