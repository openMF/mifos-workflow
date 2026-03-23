/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifos.workflow.infrastructure.usecase.cadence.starter;

import static org.mifos.workflow.infrastructure.usecase.cadence.core.CadenceFlowUsecaseConstants.MIFOS_WORKFLOW_INFRASTRUCTURE_USECASE_CADENCE_CORE_PACKAGE;
import static org.mifos.workflow.infrastructure.usecase.cadence.core.CadenceFlowUsecaseConstants.MIFOS_WORKFLOW_INFRASTRUCTURE_USECASE_CADENCE_IMPLEMENTATION_PACKAGE;
import static org.mifos.workflow.infrastructure.usecase.cadence.core.CadenceFlowUsecaseConstants.MIFOS_WORKFLOW_INFRASTRUCTURE_USECASE_CADENCE_MAPPING_PACKAGE;

import lombok.extern.slf4j.Slf4j;
import org.mifos.workflow.infrastructure.usecase.cadence.core.CadenceFlowUsecaseProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

@Slf4j
@EnableConfigurationProperties({CadenceFlowUsecaseProperties.class})
@ComponentScan(MIFOS_WORKFLOW_INFRASTRUCTURE_USECASE_CADENCE_CORE_PACKAGE)
@ComponentScan(MIFOS_WORKFLOW_INFRASTRUCTURE_USECASE_CADENCE_MAPPING_PACKAGE)
@ComponentScan(MIFOS_WORKFLOW_INFRASTRUCTURE_USECASE_CADENCE_IMPLEMENTATION_PACKAGE)
class CadenceFlowUsecaseAutoConfiguration {}
