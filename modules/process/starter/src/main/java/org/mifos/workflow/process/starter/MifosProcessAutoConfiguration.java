///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.starter;

import static org.mifos.workflow.process.core.MifosProcessConstants.MIFOS_WORKFLOW_PROCESS_CORE_PACKAGE;
import static org.mifos.workflow.process.core.MifosProcessConstants.MIFOS_WORKFLOW_PROCESS_IMPLEMENTATION_PACKAGE;
import static org.mifos.workflow.process.core.MifosProcessConstants.MIFOS_WORKFLOW_PROCESS_MAPPING_PACKAGE;

import lombok.extern.slf4j.Slf4j;
import org.mifos.workflow.process.core.MifosProcessProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

@Slf4j
@EnableConfigurationProperties({MifosProcessProperties.class})
@ComponentScan(MIFOS_WORKFLOW_PROCESS_CORE_PACKAGE)
@ComponentScan(MIFOS_WORKFLOW_PROCESS_MAPPING_PACKAGE)
@ComponentScan(MIFOS_WORKFLOW_PROCESS_IMPLEMENTATION_PACKAGE)
class MifosProcessAutoConfiguration {}
