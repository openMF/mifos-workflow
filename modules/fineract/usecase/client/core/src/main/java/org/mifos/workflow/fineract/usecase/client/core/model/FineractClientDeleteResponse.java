/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifos.workflow.fineract.usecase.client.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.mifos.commons.boot.core.model.MifosResponse;

import java.io.Serial;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
public class FineractClientDeleteResponse implements MifosResponse {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long clientId;
    private Long officeId;
    private String resourceExternalId;
    private Long resourceId;
}
