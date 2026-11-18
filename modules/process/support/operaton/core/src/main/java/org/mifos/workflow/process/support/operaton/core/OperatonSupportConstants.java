///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.support.operaton.core;

import static org.mifos.workflow.process.core.MifosProcessConstants.MIFOS_WORKFLOW_PROCESS_PACKAGE_BASE;
import static org.mifos.workflow.process.core.MifosProcessConstants.MIFOS_WORKFLOW_PROCESS_SUPPORT_PROPERTIES_PREFIX;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class OperatonSupportConstants {
    public static final String MIFOS_WORKFLOW_PROCESS_SUPPORT_OPERATON_PACKAGE_BASE =
            MIFOS_WORKFLOW_PROCESS_PACKAGE_BASE + ".support.operaton";
    public static final String MIFOS_WORKFLOW_PROCESS_SUPPORT_OPERATON_CORE_PACKAGE =
            MIFOS_WORKFLOW_PROCESS_SUPPORT_OPERATON_PACKAGE_BASE + ".core";
    public static final String MIFOS_WORKFLOW_PROCESS_SUPPORT_OPERATON_IMPLEMENTATION_PACKAGE =
            MIFOS_WORKFLOW_PROCESS_SUPPORT_OPERATON_PACKAGE_BASE + ".implementation";
    public static final String OPERATON_WORKFLOW_SUPPORT_PROPERTIES_PREFIX =
            MIFOS_WORKFLOW_PROCESS_SUPPORT_PROPERTIES_PREFIX + ".operaton";
    public static final String OPERATON_WORKFLOW_SUPPORT_PROPERTIES_ENABLED =
            OPERATON_WORKFLOW_SUPPORT_PROPERTIES_PREFIX + ".enabled";
}
