///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.usecase.flowableremote.core;

import static org.mifos.workflow.process.core.MifosProcessConstants.MIFOS_WORKFLOW_PROCESS_PACKAGE_BASE;

import lombok.experimental.UtilityClass;

/// Constants for the remote Flowable usecase implementations module.
@UtilityClass
public class FlowableRemoteUsecaseConstants {
    public static final String MIFOS_WORKFLOW_PROCESS_USECASE_FLOWABLE_REMOTE_PACKAGE_BASE =
            MIFOS_WORKFLOW_PROCESS_PACKAGE_BASE + ".usecase.flowableremote";
    public static final String MIFOS_WORKFLOW_PROCESS_USECASE_FLOWABLE_REMOTE_CORE_PACKAGE =
            MIFOS_WORKFLOW_PROCESS_USECASE_FLOWABLE_REMOTE_PACKAGE_BASE + ".core";
    public static final String MIFOS_WORKFLOW_PROCESS_USECASE_FLOWABLE_REMOTE_IMPLEMENTATION_PACKAGE =
            MIFOS_WORKFLOW_PROCESS_USECASE_FLOWABLE_REMOTE_PACKAGE_BASE + ".implementation";
}
