/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifos.workflow.infrastructure.support.cadence.implementation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mifos.boot.commons.service.MifosUsecaseJsonHelper;
import org.mifos.boot.commons.service.MifosUsecaseRegistry;
import org.mifos.workflow.infrastructure.support.cadence.core.CadenceFlowSupportProperties;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public final class CadenceMifosUsecaseoWorker {
    private final MifosUsecaseRegistry registry;
    private final MifosUsecaseJsonHelper jsonHelper;
    private final CadenceFlowSupportProperties properties;

    // TODO: implement this!
}
