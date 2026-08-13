/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifos.workflow.infrastructure.starter;

import static org.mifos.workflow.infrastructure.core.MifosFlowInfrastructureConstants.MIFOS_WORKFLOW_INFRASTRUCTURE_CORE_PACKAGE;
import static org.mifos.workflow.infrastructure.core.MifosFlowInfrastructureConstants.MIFOS_WORKFLOW_INFRASTRUCTURE_MAPPING_PACKAGE;

import lombok.extern.slf4j.Slf4j;
import org.mifos.workflow.infrastructure.core.MifosFlowInfrastructureProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

@Slf4j
@EnableConfigurationProperties({MifosFlowInfrastructureProperties.class})
@ComponentScan(MIFOS_WORKFLOW_INFRASTRUCTURE_CORE_PACKAGE)
@ComponentScan(MIFOS_WORKFLOW_INFRASTRUCTURE_MAPPING_PACKAGE)
class MifosWorkflowInfrastructureAutoConfiguration {}
