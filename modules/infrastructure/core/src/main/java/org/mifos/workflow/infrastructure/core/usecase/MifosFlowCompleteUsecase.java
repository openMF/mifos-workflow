/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifos.workflow.infrastructure.core.usecase;

import org.mifos.commons.boot.core.usecase.MifosUsecase;
import org.mifos.workflow.infrastructure.core.model.MifosFlowCompleteRequest;
import org.mifos.workflow.infrastructure.core.model.MifosFlowCompleteResponse;

public interface MifosFlowCompleteUsecase extends MifosUsecase<MifosFlowCompleteRequest, MifosFlowCompleteResponse> {}
