///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.usecase.cibsevenremote.implementation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.mifos.workflow.process.core.usecase.MifosProcessTerminateUsecase;
import org.springframework.beans.factory.annotation.Autowired;

class CibsevenRemoteTerminateUsecaseTest extends CibsevenRemoteUsecaseTestSupport {
    @Autowired
    private MifosProcessTerminateUsecase usecase;

    @Test
    void terminatesRunningProcess() {
        var key = unique("terminate");
        engine.deploy(key, CibsevenRemoteTestProcesses.userTask(key, unique("user")));
        var processId = engine.start(key);

        var response = usecase.execute(MifosProcessTerminateUsecase.Request.builder()
                .processId(processId)
                .reason("no longer needed")
                .build());

        assertThat(response.getId()).isEqualTo(CibsevenIds.toUuid(processId));
        assertThat(engine.isRunning(processId)).isFalse();
        // the REST API has no delete reason, so only the end of the process is checked
        assertThat(engine.historicProcessInstance(processId).get("endTime")).isNotNull();
    }
}
