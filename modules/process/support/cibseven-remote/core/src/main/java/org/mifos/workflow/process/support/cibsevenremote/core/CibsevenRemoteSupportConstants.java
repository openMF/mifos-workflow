///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.support.cibsevenremote.core;

import static org.mifos.workflow.process.core.MifosProcessConstants.MIFOS_WORKFLOW_PROCESS_PACKAGE_BASE;
import static org.mifos.workflow.process.core.MifosProcessConstants.MIFOS_WORKFLOW_PROCESS_SUPPORT_PROPERTIES_PREFIX;

import lombok.experimental.UtilityClass;

/// Constants for the remote CIB seven support module.
@UtilityClass
public final class CibsevenRemoteSupportConstants {
    public static final String MIFOS_WORKFLOW_PROCESS_SUPPORT_CIBSEVEN_REMOTE_PACKAGE_BASE =
            MIFOS_WORKFLOW_PROCESS_PACKAGE_BASE + ".support.cibsevenremote";
    public static final String MIFOS_WORKFLOW_PROCESS_SUPPORT_CIBSEVEN_REMOTE_CORE_PACKAGE =
            MIFOS_WORKFLOW_PROCESS_SUPPORT_CIBSEVEN_REMOTE_PACKAGE_BASE + ".core";
    public static final String MIFOS_WORKFLOW_PROCESS_SUPPORT_CIBSEVEN_REMOTE_IMPLEMENTATION_PACKAGE =
            MIFOS_WORKFLOW_PROCESS_SUPPORT_CIBSEVEN_REMOTE_PACKAGE_BASE + ".implementation";
    public static final String CIBSEVEN_REMOTE_WORKFLOW_SUPPORT_PROPERTIES_PREFIX =
            MIFOS_WORKFLOW_PROCESS_SUPPORT_PROPERTIES_PREFIX + ".cibseven-remote";
    public static final String CIBSEVEN_REMOTE_WORKFLOW_SUPPORT_PROPERTIES_ENABLED =
            CIBSEVEN_REMOTE_WORKFLOW_SUPPORT_PROPERTIES_PREFIX + ".enabled";
    public static final String CIBSEVEN_REMOTE_WORKFLOW_SUPPORT_PROPERTIES_BASE_URL =
            CIBSEVEN_REMOTE_WORKFLOW_SUPPORT_PROPERTIES_PREFIX + ".base-url";
    public static final String CIBSEVEN_REMOTE_WORKFLOW_SUPPORT_PROPERTIES_USERNAME =
            CIBSEVEN_REMOTE_WORKFLOW_SUPPORT_PROPERTIES_PREFIX + ".username";
    public static final String CIBSEVEN_REMOTE_WORKFLOW_SUPPORT_PROPERTIES_PASSWORD =
            CIBSEVEN_REMOTE_WORKFLOW_SUPPORT_PROPERTIES_PREFIX + ".password";
    /// Name of the extension property on the service task that holds the flow input or usecase request type.
    public static final String CIBSEVEN_REMOTE_WORKFLOW_SUPPORT_EXTENSION_PROPERTY_TYPE = "type";
}
