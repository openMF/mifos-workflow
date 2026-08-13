/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifos.workflow.infrastructure.usecase.operaton.core;

import static org.mifos.workflow.infrastructure.usecase.operaton.core.OperatonFlowUsecaseConstants.OPERATON_WORKFLOW_PROPERTIES_PREFIX;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = OPERATON_WORKFLOW_PROPERTIES_PREFIX)
public class OperatonFlowUsecaseProperties {
    @Builder.Default
    private Boolean enabled = true;
}
