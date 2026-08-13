/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifos.workflow.fineract.usecase.client.core;

import lombok.experimental.UtilityClass;

import static org.mifos.commons.boot.core.MifosConstants.MIFOS_PACKAGE_BASE;
import static org.mifos.commons.boot.core.MifosConstants.MIFOS_PROPERTIES_PREFIX;

@UtilityClass
public class FineractClientConstants {
    public static final String MIFOS_WORKFLOW_FINERACT_USECASE_CLIENT_PACKAGE_BASE = MIFOS_PACKAGE_BASE + ".workflow.fineract.usecase.client";
    public static final String MIFOS_WORKFLOW_FINERACT_USECASE_CLIENT_CORE_PACKAGE = MIFOS_WORKFLOW_FINERACT_USECASE_CLIENT_PACKAGE_BASE + ".core";
    public static final String MIFOS_WORKFLOW_FINERACT_USECASE_CLIENT_IMPLEMENTATION_PACKAGE = MIFOS_WORKFLOW_FINERACT_USECASE_CLIENT_PACKAGE_BASE + ".implementation";
    public static final String MIFOS_WORKFLOW_FINERACT_USECASE_CLIENT_PROPERTIES_PREFIX = MIFOS_PROPERTIES_PREFIX + ".workflow.fineract.usecase.client";
    public static final String MIFOS_WORKFLOW_FINERACT_USECASE_CLIENT_PROPERTIES_ENABLED =
            MIFOS_WORKFLOW_FINERACT_USECASE_CLIENT_PROPERTIES_PREFIX + ".enabled";
}
