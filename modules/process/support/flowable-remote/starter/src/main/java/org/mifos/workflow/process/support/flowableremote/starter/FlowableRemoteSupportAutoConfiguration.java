///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.support.flowableremote.starter;

import static org.mifos.workflow.process.core.MifosProcessConstants.MIFOS_WORKFLOW_PROCESS_PROPERTIES_ENGINE;
import static org.mifos.workflow.process.support.flowableremote.core.FlowableRemoteSupportConstants.MIFOS_WORKFLOW_PROCESS_SUPPORT_FLOWABLE_REMOTE_IMPLEMENTATION_PACKAGE;

import lombok.extern.slf4j.Slf4j;
import org.mifos.workflow.process.support.flowableremote.core.FlowableRemoteSupportProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@Slf4j
@EnableConfigurationProperties(FlowableRemoteSupportProperties.class)
@ComponentScan(MIFOS_WORKFLOW_PROCESS_SUPPORT_FLOWABLE_REMOTE_IMPLEMENTATION_PACKAGE)
@ConditionalOnProperty(name = MIFOS_WORKFLOW_PROCESS_PROPERTIES_ENGINE, havingValue = "FLOWABLE_REMOTE")
@Import({FlowableRemoteClientConfiguration.class})
class FlowableRemoteSupportAutoConfiguration {}
