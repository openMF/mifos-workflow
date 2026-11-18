///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.core;

import static org.mifos.workflow.process.core.MifosProcessConstants.MIFOS_WORKFLOW_PROCESS_PROPERTIES_PREFIX;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/// Configuration properties for the workflow process module.
///
/// @param enabled reserved, not used yet
/// @param engine the workflow engine the application uses; `CIBSEVEN` by default
@ConfigurationProperties(prefix = MIFOS_WORKFLOW_PROCESS_PROPERTIES_PREFIX)
public record MifosProcessProperties(
        @DefaultValue("true") Boolean enabled,
        @DefaultValue("CIBSEVEN") Engine engine) {
    /// The workflow engines.
    public enum Engine {
        CIBSEVEN,
        CIBSEVEN_REMOTE,
        EXIMEE,
        OPERATON,
        FLOWABLE,
        FLOWABLE_REMOTE,
        CAMUNDA,
        CONDUCTOR
    }
}
