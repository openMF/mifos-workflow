///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.support.camunda.core;

import static org.mifos.workflow.process.core.MifosProcessConstants.MIFOS_WORKFLOW_PROCESS_PACKAGE_BASE;
import static org.mifos.workflow.process.core.MifosProcessConstants.MIFOS_WORKFLOW_PROCESS_SUPPORT_PROPERTIES_PREFIX;

import lombok.experimental.UtilityClass;

/// Constants for the Camunda support module.
@UtilityClass
public final class CamundaSupportConstants {
    public static final String MIFOS_WORKFLOW_PROCESS_SUPPORT_CAMUNDA_PACKAGE_BASE =
            MIFOS_WORKFLOW_PROCESS_PACKAGE_BASE + ".support.camunda";
    public static final String MIFOS_WORKFLOW_PROCESS_SUPPORT_CAMUNDA_CORE_PACKAGE =
            MIFOS_WORKFLOW_PROCESS_SUPPORT_CAMUNDA_PACKAGE_BASE + ".core";
    public static final String MIFOS_WORKFLOW_PROCESS_SUPPORT_CAMUNDA_IMPLEMENTATION_PACKAGE =
            MIFOS_WORKFLOW_PROCESS_SUPPORT_CAMUNDA_PACKAGE_BASE + ".implementation";
    public static final String CAMUNDA_WORKFLOW_SUPPORT_PROPERTIES_PREFIX =
            MIFOS_WORKFLOW_PROCESS_SUPPORT_PROPERTIES_PREFIX + ".camunda";
    public static final String CAMUNDA_WORKFLOW_SUPPORT_PROPERTIES_ENABLED =
            CAMUNDA_WORKFLOW_SUPPORT_PROPERTIES_PREFIX + ".enabled";
    /// Task header naming the flow input or usecase request class a `mifosAdapter` service task dispatches to.
    public static final String CAMUNDA_WORKFLOW_SUPPORT_TYPE_HEADER = "type";
}
