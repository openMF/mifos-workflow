///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.support.cibseven.core;

import static org.mifos.workflow.process.support.cibseven.core.CibsevenSupportConstants.CIBSEVEN_WORKFLOW_SUPPORT_PROPERTIES_PREFIX;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/// Configuration properties for the CIB seven support module.
@ConfigurationProperties(prefix = CIBSEVEN_WORKFLOW_SUPPORT_PROPERTIES_PREFIX)
public record CibsevenSupportProperties(
        @DefaultValue("input") String inputVariable,
        @DefaultValue("output") String outputVariable,
        @DefaultValue("true") boolean failOnNullInput,
        @DefaultValue("true") boolean serializeResult) {}
