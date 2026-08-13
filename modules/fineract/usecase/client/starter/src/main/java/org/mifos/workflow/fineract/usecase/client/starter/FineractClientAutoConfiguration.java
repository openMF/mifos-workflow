/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifos.workflow.fineract.usecase.client.starter;

import lombok.extern.slf4j.Slf4j;
import org.mifos.workflow.fineract.usecase.client.core.FineractClientProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

import static org.mifos.workflow.fineract.usecase.client.core.FineractClientConstants.MIFOS_WORKFLOW_FINERACT_USECASE_CLIENT_CORE_PACKAGE;
import static org.mifos.workflow.fineract.usecase.client.core.FineractClientConstants.MIFOS_WORKFLOW_FINERACT_USECASE_CLIENT_IMPLEMENTATION_PACKAGE;

@Slf4j
@EnableConfigurationProperties({FineractClientProperties.class})
@ComponentScan(MIFOS_WORKFLOW_FINERACT_USECASE_CLIENT_CORE_PACKAGE)
@ComponentScan(MIFOS_WORKFLOW_FINERACT_USECASE_CLIENT_IMPLEMENTATION_PACKAGE)
class FineractClientAutoConfiguration {}
