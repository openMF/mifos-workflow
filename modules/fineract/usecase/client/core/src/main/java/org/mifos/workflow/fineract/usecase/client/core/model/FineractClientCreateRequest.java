/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifos.workflow.fineract.usecase.client.core.model;

import java.io.Serial;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.mifos.commons.boot.core.model.MifosRequest;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
public class FineractClientCreateRequest implements MifosRequest {
    @Serial
    private static final long serialVersionUID = 1L;

    private String activationDate;
    private Boolean active;
    private String dateFormat;
    private LocalDate dateOfBirth;
    private String emailAddress;
    private String externalId;
    private String firstname;
    private String fullname;
    private Long groupId;
    private String lastname;
    private Long legalFormId;
    private String locale;
    private String middlename;
    private String mobileNo;
    private Long officeId;
}
