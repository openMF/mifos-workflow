/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifos.workflow.infrastructure.support.conductor.starter;

import static org.mifos.workflow.infrastructure.support.conductor.core.ConductorFlowSupportConstants.MIFOS_WORKFLOW_INFRASTRUCTURE_SUPPORT_CONDUCTOR_CORE_PACKAGE;
import static org.mifos.workflow.infrastructure.support.conductor.core.ConductorFlowSupportConstants.MIFOS_WORKFLOW_INFRASTRUCTURE_SUPPORT_CONDUCTOR_IMPLEMENTATION_PACKAGE;

import lombok.extern.slf4j.Slf4j;
import org.mifos.workflow.infrastructure.support.conductor.core.ConductorFlowSupportProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

@Slf4j
@EnableConfigurationProperties({ConductorFlowSupportProperties.class})
@ComponentScan(MIFOS_WORKFLOW_INFRASTRUCTURE_SUPPORT_CONDUCTOR_CORE_PACKAGE)
@ComponentScan(MIFOS_WORKFLOW_INFRASTRUCTURE_SUPPORT_CONDUCTOR_IMPLEMENTATION_PACKAGE)
class ConductorFlowSupportAutoConfiguration {}
