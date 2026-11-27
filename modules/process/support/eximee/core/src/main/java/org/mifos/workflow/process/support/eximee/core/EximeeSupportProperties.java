///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.support.eximee.core;

import static org.mifos.workflow.process.support.eximee.core.EximeeSupportConstants.EXIMEE_WORKFLOW_SUPPORT_PROPERTIES_PREFIX;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/// Configuration properties for the Eximee support module.
@ConfigurationProperties(prefix = EXIMEE_WORKFLOW_SUPPORT_PROPERTIES_PREFIX)
public record EximeeSupportProperties(
        @DefaultValue("input") String inputVariable,
        @DefaultValue("output") String outputVariable,
        @DefaultValue("true") boolean failOnNullInput,
        @DefaultValue("true") boolean serializeResult) {}
