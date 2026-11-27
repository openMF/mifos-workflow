///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.core;

import static org.mifos.commons.boot.core.MifosCommonsBootConstants.MIFOS_COMMONS_BOOT_ERROR_CODE_EXTERNAL_INCREMENT;
import static org.mifos.commons.boot.core.MifosCommonsBootConstants.MIFOS_COMMONS_BOOT_ERROR_CODE_EXTERNAL_START;
import static org.mifos.commons.boot.core.MifosConstants.MIFOS_MESSAGE_BASE;
import static org.mifos.commons.boot.core.MifosConstants.MIFOS_PACKAGE_BASE;
import static org.mifos.commons.boot.core.MifosConstants.MIFOS_PROPERTIES_PREFIX;

import lombok.experimental.UtilityClass;

/// Constants for the workflow process module.
@UtilityClass
public final class MifosProcessConstants {
    public static final String MIFOS_WORKFLOW_PROCESS_MESSAGE_BASE = MIFOS_MESSAGE_BASE + "/workflow";
    public static final String MIFOS_WORKFLOW_PROCESS_MESSAGE_PREFIX = MIFOS_PACKAGE_BASE + ".workflow.process";
    public static final String MIFOS_WORKFLOW_PROCESS_MESSAGE_ERROR_PREFIX =
            MIFOS_WORKFLOW_PROCESS_MESSAGE_PREFIX + ".error";
    public static final String MIFOS_WORKFLOW_PROCESS_PACKAGE_BASE = MIFOS_PACKAGE_BASE + ".workflow.process";
    public static final String MIFOS_WORKFLOW_PROCESS_CORE_PACKAGE = MIFOS_WORKFLOW_PROCESS_PACKAGE_BASE + ".core";
    public static final String MIFOS_WORKFLOW_PROCESS_IMPLEMENTATION_PACKAGE =
            MIFOS_WORKFLOW_PROCESS_PACKAGE_BASE + ".implementation";
    public static final String MIFOS_WORKFLOW_PROCESS_MAPPING_PACKAGE =
            MIFOS_WORKFLOW_PROCESS_PACKAGE_BASE + ".mapping";
    public static final String MIFOS_WORKFLOW_PROCESS_PROPERTIES_PREFIX = MIFOS_PROPERTIES_PREFIX + ".workflow.process";
    public static final String MIFOS_WORKFLOW_PROCESS_SUPPORT_PROPERTIES_PREFIX =
            MIFOS_WORKFLOW_PROCESS_PROPERTIES_PREFIX + ".support";
    /// Name of the bean every in-process engine adapter registers, so BPMN models call
    /// `${mifosDispatcher.execute(execution, '<request type>')}` regardless of the engine.
    public static final String MIFOS_WORKFLOW_PROCESS_ADAPTER_BEAN = "mifosAdapter";
    public static final String MIFOS_WORKFLOW_PROCESS_PROPERTIES_ENABLED =
            MIFOS_WORKFLOW_PROCESS_PROPERTIES_PREFIX + ".enabled";
    public static final String MIFOS_WORKFLOW_PROCESS_PROPERTIES_ENGINE =
            MIFOS_WORKFLOW_PROCESS_PROPERTIES_PREFIX + ".engine";
    public static final int MIFOS_WORKFLOW_PROCESS_ERROR_CODE_START =
            MIFOS_COMMONS_BOOT_ERROR_CODE_EXTERNAL_START + 10000;
    public static final int MIFOS_WORKFLOW_PROCESS_ERROR_CODE_INCREMENT =
            MIFOS_COMMONS_BOOT_ERROR_CODE_EXTERNAL_INCREMENT;
}
