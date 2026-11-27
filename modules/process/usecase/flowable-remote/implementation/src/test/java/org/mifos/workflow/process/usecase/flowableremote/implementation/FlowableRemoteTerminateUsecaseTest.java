///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.usecase.flowableremote.implementation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.mifos.workflow.process.core.usecase.MifosProcessTerminateUsecase;
import org.springframework.beans.factory.annotation.Autowired;

class FlowableRemoteTerminateUsecaseTest extends FlowableRemoteUsecaseTestSupport {
    @Autowired
    private MifosProcessTerminateUsecase usecase;

    @Test
    void terminatesRunningProcess() {
        var key = unique("terminate");
        engine.deploy(key, FlowableRemoteTestProcesses.userTask(key, unique("user")));
        var processId = engine.start(key);

        var response = usecase.execute(MifosProcessTerminateUsecase.Request.builder()
                .processId(processId)
                .reason("no longer needed")
                .build());

        assertThat(response.getId()).isEqualTo(FlowableIds.toUuid(processId));
        assertThat(engine.isRunning(processId)).isFalse();
        var historic = engine.historicProcessInstance(processId);
        assertThat(historic.get("endTime")).isNotNull();
        assertThat(historic.get("deleteReason")).isEqualTo("no longer needed");
    }
}
