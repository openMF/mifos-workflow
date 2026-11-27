///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.usecase.cibsevenremote.core;

import static org.mifos.workflow.process.core.MifosProcessConstants.MIFOS_WORKFLOW_PROCESS_PACKAGE_BASE;

import lombok.experimental.UtilityClass;

/// Constants for the remote CIB seven usecase implementations module.
@UtilityClass
public class CibsevenRemoteUsecaseConstants {
    public static final String MIFOS_WORKFLOW_PROCESS_USECASE_CIBSEVEN_REMOTE_PACKAGE_BASE =
            MIFOS_WORKFLOW_PROCESS_PACKAGE_BASE + ".usecase.cibsevenremote";
    public static final String MIFOS_WORKFLOW_PROCESS_USECASE_CIBSEVEN_REMOTE_CORE_PACKAGE =
            MIFOS_WORKFLOW_PROCESS_USECASE_CIBSEVEN_REMOTE_PACKAGE_BASE + ".core";
    public static final String MIFOS_WORKFLOW_PROCESS_USECASE_CIBSEVEN_REMOTE_IMPLEMENTATION_PACKAGE =
            MIFOS_WORKFLOW_PROCESS_USECASE_CIBSEVEN_REMOTE_PACKAGE_BASE + ".implementation";
}
