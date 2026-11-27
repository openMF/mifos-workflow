///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.service.dispatch.core;

import static org.mifos.workflow.process.core.MifosProcessConstants.MIFOS_WORKFLOW_PROCESS_PACKAGE_BASE;

import lombok.experimental.UtilityClass;

/// Constants for the process dispatch service module.
@UtilityClass
public final class MifosProcessDispatchConstants {
    public static final String MIFOS_WORKFLOW_PROCESS_SERVICE_DISPATCH_PACKAGE_BASE =
            MIFOS_WORKFLOW_PROCESS_PACKAGE_BASE + ".service.dispatch";
    public static final String MIFOS_WORKFLOW_PROCESS_SERVICE_DISPATCH_CORE_PACKAGE =
            MIFOS_WORKFLOW_PROCESS_SERVICE_DISPATCH_PACKAGE_BASE + ".core";
    public static final String MIFOS_WORKFLOW_PROCESS_SERVICE_DISPATCH_IMPLEMENTATION_PACKAGE =
            MIFOS_WORKFLOW_PROCESS_SERVICE_DISPATCH_PACKAGE_BASE + ".implementation";
}
