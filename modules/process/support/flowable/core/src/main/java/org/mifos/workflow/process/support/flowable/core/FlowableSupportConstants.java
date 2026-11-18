///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.support.flowable.core;

import static org.mifos.workflow.process.core.MifosProcessConstants.MIFOS_WORKFLOW_PROCESS_PACKAGE_BASE;
import static org.mifos.workflow.process.core.MifosProcessConstants.MIFOS_WORKFLOW_PROCESS_SUPPORT_PROPERTIES_PREFIX;

import lombok.experimental.UtilityClass;

/// Constants for the Flowable support module.
@UtilityClass
public final class FlowableSupportConstants {
    public static final String MIFOS_WORKFLOW_PROCESS_SUPPORT_FLOWABLE_PACKAGE_BASE =
            MIFOS_WORKFLOW_PROCESS_PACKAGE_BASE + ".support.flowable";
    public static final String MIFOS_WORKFLOW_PROCESS_SUPPORT_FLOWABLE_CORE_PACKAGE =
            MIFOS_WORKFLOW_PROCESS_SUPPORT_FLOWABLE_PACKAGE_BASE + ".core";
    public static final String MIFOS_WORKFLOW_PROCESS_SUPPORT_FLOWABLE_IMPLEMENTATION_PACKAGE =
            MIFOS_WORKFLOW_PROCESS_SUPPORT_FLOWABLE_PACKAGE_BASE + ".implementation";
    public static final String FLOWABLE_WORKFLOW_SUPPORT_PROPERTIES_PREFIX =
            MIFOS_WORKFLOW_PROCESS_SUPPORT_PROPERTIES_PREFIX + ".flowable";
    public static final String FLOWABLE_WORKFLOW_SUPPORT_PROPERTIES_ENABLED =
            FLOWABLE_WORKFLOW_SUPPORT_PROPERTIES_PREFIX + ".enabled";
}
