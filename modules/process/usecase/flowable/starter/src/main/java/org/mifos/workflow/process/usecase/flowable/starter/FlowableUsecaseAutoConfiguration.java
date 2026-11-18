///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.usecase.flowable.starter;

import static org.mifos.workflow.process.core.MifosProcessConstants.MIFOS_WORKFLOW_PROCESS_PROPERTIES_ENGINE;
import static org.mifos.workflow.process.usecase.flowable.core.FlowableUsecaseConstants.MIFOS_WORKFLOW_PROCESS_USECASE_FLOWABLE_CORE_PACKAGE;
import static org.mifos.workflow.process.usecase.flowable.core.FlowableUsecaseConstants.MIFOS_WORKFLOW_PROCESS_USECASE_FLOWABLE_IMPLEMENTATION_PACKAGE;
import static org.mifos.workflow.process.usecase.flowable.core.FlowableUsecaseConstants.MIFOS_WORKFLOW_PROCESS_USECASE_FLOWABLE_MAPPING_PACKAGE;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;

@Slf4j
@ComponentScan(MIFOS_WORKFLOW_PROCESS_USECASE_FLOWABLE_CORE_PACKAGE)
@ComponentScan(MIFOS_WORKFLOW_PROCESS_USECASE_FLOWABLE_MAPPING_PACKAGE)
@ComponentScan(MIFOS_WORKFLOW_PROCESS_USECASE_FLOWABLE_IMPLEMENTATION_PACKAGE)
@ConditionalOnProperty(name = MIFOS_WORKFLOW_PROCESS_PROPERTIES_ENGINE, havingValue = "FLOWABLE")
class FlowableUsecaseAutoConfiguration {}
