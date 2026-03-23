/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifos.workflow.infrastructure.support.eximee.starter;

import static org.mifos.workflow.infrastructure.support.eximee.core.EximeeFlowSupportConstants.MIFOS_WORKFLOW_INFRASTRUCTURE_SUPPORT_EXIMEE_CORE_PACKAGE;
import static org.mifos.workflow.infrastructure.support.eximee.core.EximeeFlowSupportConstants.MIFOS_WORKFLOW_INFRASTRUCTURE_SUPPORT_EXIMEE_IMPLEMENTATION_PACKAGE;

import lombok.extern.slf4j.Slf4j;
import org.mifos.workflow.infrastructure.support.eximee.core.EximeeFlowSupportProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

@Slf4j
@EnableConfigurationProperties({EximeeFlowSupportProperties.class})
@ComponentScan(MIFOS_WORKFLOW_INFRASTRUCTURE_SUPPORT_EXIMEE_CORE_PACKAGE)
@ComponentScan(MIFOS_WORKFLOW_INFRASTRUCTURE_SUPPORT_EXIMEE_IMPLEMENTATION_PACKAGE)
class EximeeFlowSupportAutoConfiguration {}
