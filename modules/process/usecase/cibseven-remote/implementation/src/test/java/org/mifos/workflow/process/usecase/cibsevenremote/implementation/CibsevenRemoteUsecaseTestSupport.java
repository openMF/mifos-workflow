///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.usecase.cibsevenremote.implementation;

import static org.mifos.workflow.process.core.MifosProcessConstants.MIFOS_WORKFLOW_PROCESS_PROPERTIES_ENGINE;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/// Shared setup for the remote CIB seven usecase tests. All test classes share one cached Spring context, so one
/// engine container; tests isolate themselves by using unique process keys, assignees and signal names.
@SpringBootTest(
        classes = CibsevenRemoteTestApplication.class,
        properties = MIFOS_WORKFLOW_PROCESS_PROPERTIES_ENGINE + "=CIBSEVEN_REMOTE")
abstract class CibsevenRemoteUsecaseTestSupport {
    @Autowired
    protected CibsevenRemoteTestEngine engine;

    protected static String unique(String prefix) {
        return prefix + "-" + UUID.randomUUID();
    }
}
