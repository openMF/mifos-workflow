/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifos.workflow.infrastructure.support.cibseven.core;

import static org.mifos.workflow.infrastructure.support.cibseven.core.CibsevenFlowSupportConstants.CIBSEVEN_WORKFLOW_SUPPORT_PROPERTIES_PREFIX;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = CIBSEVEN_WORKFLOW_SUPPORT_PROPERTIES_PREFIX)
public class CibsevenFlowSupportProperties {
    @Builder.Default
    private boolean enabled = true;

    @Builder.Default
    private String inputVariable = "input";

    @Builder.Default
    private String outputVariable = "output";

    @Builder.Default
    private String typeOverrideVariable = "_type";

    @Builder.Default
    private boolean failOnNullInput = true;

    @Builder.Default
    private boolean serializeResult = true;
}
