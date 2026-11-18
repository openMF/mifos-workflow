///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.transport.rest.core;

import static org.mifos.workflow.process.transport.rest.core.MifosProcessTransportRestConstants.MIFOS_WORKFLOW_PROCESS_TRANSPORT_REST_PROPERTIES_PREFIX;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/// Configuration properties for the workflow process REST transport.
@ConfigurationProperties(prefix = MIFOS_WORKFLOW_PROCESS_TRANSPORT_REST_PROPERTIES_PREFIX)
public record MifosProcessTransportRestProperties(
        @DefaultValue("true") Boolean enabled) {}
