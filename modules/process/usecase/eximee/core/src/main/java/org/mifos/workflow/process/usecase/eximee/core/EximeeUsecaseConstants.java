///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.usecase.eximee.core;

import static org.mifos.workflow.process.core.MifosProcessConstants.MIFOS_WORKFLOW_PROCESS_PACKAGE_BASE;
import static org.mifos.workflow.process.core.MifosProcessConstants.MIFOS_WORKFLOW_PROCESS_PROPERTIES_PREFIX;

import lombok.experimental.UtilityClass;

/// Constants for the Eximee usecase implementations module.
@UtilityClass
public final class EximeeUsecaseConstants {
    public static final String MIFOS_WORKFLOW_PROCESS_USECASE_EXIMEE_PACKAGE_BASE =
            MIFOS_WORKFLOW_PROCESS_PACKAGE_BASE + ".usecase.eximee";
    public static final String MIFOS_WORKFLOW_PROCESS_USECASE_EXIMEE_CORE_PACKAGE =
            MIFOS_WORKFLOW_PROCESS_USECASE_EXIMEE_PACKAGE_BASE + ".core";
    public static final String MIFOS_WORKFLOW_PROCESS_USECASE_EXIMEE_MAPPING_PACKAGE =
            MIFOS_WORKFLOW_PROCESS_USECASE_EXIMEE_PACKAGE_BASE + ".mapping";
    public static final String MIFOS_WORKFLOW_PROCESS_USECASE_EXIMEE_IMPLEMENTATION_PACKAGE =
            MIFOS_WORKFLOW_PROCESS_USECASE_EXIMEE_PACKAGE_BASE + ".implementation";
    public static final String EXIMEE_WORKFLOW_PROPERTIES_PREFIX = MIFOS_WORKFLOW_PROCESS_PROPERTIES_PREFIX + ".eximee";
    public static final String EXIMEE_WORKFLOW_PROPERTIES_ENABLED = EXIMEE_WORKFLOW_PROPERTIES_PREFIX + ".enabled";
}
