/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifos.workflow.infrastructure.core;

import static org.mifos.commons.boot.core.MifosCommonsBootConstants.MIFOS_COMMONS_BOOT_ERROR_CODE_CUSTOM_INCREMENT;
import static org.mifos.commons.boot.core.MifosCommonsBootConstants.MIFOS_COMMONS_BOOT_ERROR_CODE_CUSTOM_START;
import static org.mifos.commons.boot.core.MifosConstants.MIFOS_MESSAGE_BASE;
import static org.mifos.commons.boot.core.MifosConstants.MIFOS_PACKAGE_BASE;
import static org.mifos.commons.boot.core.MifosConstants.MIFOS_PROPERTIES_PREFIX;

import lombok.experimental.UtilityClass;

@UtilityClass
public class MifosFlowInfrastructureConstants {
    public static final String MIFOS_WORKFLOW_INFRASTRUCTURE_MESSAGE_BASE = MIFOS_MESSAGE_BASE + "/workflow";
    public static final String MIFOS_WORKFLOW_INFRASTRUCTURE_MESSAGE_PREFIX =
            MIFOS_PACKAGE_BASE + ".workflow.infrastructure";
    public static final String MIFOS_WORKFLOW_INFRASTRUCTURE_MESSAGE_ERROR_PREFIX =
            MIFOS_WORKFLOW_INFRASTRUCTURE_MESSAGE_PREFIX + ".error";
    public static final String MIFOS_WORKFLOW_INFRASTRUCTURE_PACKAGE_BASE = MIFOS_PACKAGE_BASE + ".infrastructure";
    public static final String MIFOS_WORKFLOW_INFRASTRUCTURE_CORE_PACKAGE =
            MIFOS_WORKFLOW_INFRASTRUCTURE_PACKAGE_BASE + ".core";
    public static final String MIFOS_WORKFLOW_INFRASTRUCTURE_MAPPING_PACKAGE =
            MIFOS_WORKFLOW_INFRASTRUCTURE_PACKAGE_BASE + ".mapping";
    public static final String MIFOS_WORKFLOW_INFRASTRUCTURE_PROPERTIES_PREFIX =
            MIFOS_PROPERTIES_PREFIX + ".workflow.infrastructure";
    public static final String MIFOS_WORKFLOW_INFRASTRUCTURE_SUPPORT_PROPERTIES_PREFIX =
            MIFOS_WORKFLOW_INFRASTRUCTURE_PROPERTIES_PREFIX + ".support";
    public static final String MIFOS_WORKFLOW_INFRASTRUCTURE_PROPERTIES_ENABLED =
            MIFOS_WORKFLOW_INFRASTRUCTURE_PROPERTIES_PREFIX + ".enabled";
    public static final int MIFOS_WORKFLOW_INFRASTRUCTURE_ERROR_CODE_START =
            MIFOS_COMMONS_BOOT_ERROR_CODE_CUSTOM_START + 10000;
    public static final int MIFOS_WORKFLOW_INFRASTRUCTURE_ERROR_CODE_INCREMENT =
            MIFOS_COMMONS_BOOT_ERROR_CODE_CUSTOM_INCREMENT;
}
