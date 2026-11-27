///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.service.dispatch.starter;

import static org.mifos.workflow.process.service.dispatch.core.MifosProcessDispatchConstants.MIFOS_WORKFLOW_PROCESS_SERVICE_DISPATCH_CORE_PACKAGE;
import static org.mifos.workflow.process.service.dispatch.core.MifosProcessDispatchConstants.MIFOS_WORKFLOW_PROCESS_SERVICE_DISPATCH_IMPLEMENTATION_PACKAGE;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.ComponentScan;

/// Registers the default `MifosProcessDispatcher`, which routes process steps to `MifosFlow` and `MifosUsecase` beans
/// by input type.
@Slf4j
@ComponentScan(MIFOS_WORKFLOW_PROCESS_SERVICE_DISPATCH_CORE_PACKAGE)
@ComponentScan(MIFOS_WORKFLOW_PROCESS_SERVICE_DISPATCH_IMPLEMENTATION_PACKAGE)
class MifosProcessDispatchAutoConfiguration {}
