/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifos.workflow.fineract.usecase.client.core.usecase;

import org.mifos.commons.boot.core.usecase.MifosUsecase;
import org.mifos.workflow.fineract.usecase.client.core.model.FineractClientCreateRequest;
import org.mifos.workflow.fineract.usecase.client.core.model.FineractClientCreateResponse;

public interface FineractClientCreateUsecase
        extends MifosUsecase<FineractClientCreateRequest, FineractClientCreateResponse> {}
