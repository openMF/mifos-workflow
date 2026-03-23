/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifos.workflow.fineract.usecase.client.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mifos.boot.commons.mapping.MifosMapperConfiguration;
import org.mifos.commons.oas.imperative.sdk.fineract.implementation.models.DeleteClientsClientIdResponse;
import org.mifos.commons.oas.imperative.sdk.fineract.implementation.models.PostClientsRequest;
import org.mifos.commons.oas.imperative.sdk.fineract.implementation.models.PostClientsResponse;
import org.mifos.workflow.fineract.usecase.client.core.model.FineractClientCreateRequest;
import org.mifos.workflow.fineract.usecase.client.core.model.FineractClientCreateResponse;
import org.mifos.workflow.fineract.usecase.client.core.model.FineractClientDeleteResponse;

@Mapper(config = MifosMapperConfiguration.class)
public interface FineractClientRequestMapper {
    @Mapping(ignore = true, target = "address")
    @Mapping(ignore = true, target = "datatables")
    PostClientsRequest map(FineractClientCreateRequest source);

    FineractClientCreateResponse map(PostClientsResponse source);

    FineractClientDeleteResponse map(DeleteClientsClientIdResponse source);
}
