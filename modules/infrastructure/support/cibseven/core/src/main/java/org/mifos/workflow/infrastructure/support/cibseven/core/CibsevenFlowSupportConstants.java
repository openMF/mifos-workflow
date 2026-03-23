/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifos.workflow.infrastructure.support.cibseven.core;

import static org.mifos.workflow.infrastructure.core.MifosFlowInfrastructureConstants.MIFOS_WORKFLOW_INFRASTRUCTURE_PACKAGE_BASE;
import static org.mifos.workflow.infrastructure.core.MifosFlowInfrastructureConstants.MIFOS_WORKFLOW_INFRASTRUCTURE_SUPPORT_PROPERTIES_PREFIX;

import lombok.experimental.UtilityClass;

@UtilityClass
public class CibsevenFlowSupportConstants {
    public static final String MIFOS_WORKFLOW_INFRASTRUCTURE_SUPPORT_CIBSEVEN_PACKAGE_BASE =
            MIFOS_WORKFLOW_INFRASTRUCTURE_PACKAGE_BASE + ".support.cibseven";
    public static final String MIFOS_WORKFLOW_INFRASTRUCTURE_SUPPORT_CIBSEVEN_CORE_PACKAGE =
            MIFOS_WORKFLOW_INFRASTRUCTURE_SUPPORT_CIBSEVEN_PACKAGE_BASE + ".core";
    public static final String MIFOS_WORKFLOW_INFRASTRUCTURE_SUPPORT_CIBSEVEN_IMPLEMENTATION_PACKAGE =
            MIFOS_WORKFLOW_INFRASTRUCTURE_SUPPORT_CIBSEVEN_PACKAGE_BASE + ".implementation";
    public static final String CIBSEVEN_WORKFLOW_SUPPORT_PROPERTIES_PREFIX =
            MIFOS_WORKFLOW_INFRASTRUCTURE_SUPPORT_PROPERTIES_PREFIX + "cibseven";
    public static final String CIBSEVEN_WORKFLOW_SUPPORT_PROPERTIES_ENABLED =
            CIBSEVEN_WORKFLOW_SUPPORT_PROPERTIES_PREFIX + ".enabled";
}
