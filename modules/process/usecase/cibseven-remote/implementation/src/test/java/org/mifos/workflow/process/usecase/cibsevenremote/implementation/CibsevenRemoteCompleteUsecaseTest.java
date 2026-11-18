///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.usecase.cibsevenremote.implementation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mifos.workflow.process.core.usecase.MifosProcessCompleteUsecase;
import org.springframework.beans.factory.annotation.Autowired;

final class CibsevenRemoteCompleteUsecaseTest extends CibsevenRemoteUsecaseTestSupport {
    @Autowired
    private MifosProcessCompleteUsecase usecase;

    @Test
    void completesTaskAndFinishesProcess() {
        var key = unique("complete");
        engine.deploy(key, CibsevenRemoteTestProcesses.userTask(key, unique("user")));
        var processId = engine.start(key);
        var taskId = (String) engine.tasks(processId).getFirst().get("id");

        var response = usecase.execute(MifosProcessCompleteUsecase.Request.builder()
                .taskId(taskId)
                .variables(Map.of("approved", true))
                .build());

        assertThat(response.getId()).isEqualTo(CibsevenIds.toUuid(taskId));
        assertThat(engine.taskCount(taskId)).isZero();
        assertThat(engine.historicProcessInstance(processId).get("endTime")).isNotNull();
        assertThat(engine.historicVariables(processId)).containsEntry("approved", true);
    }

    @Test
    void completesTaskWithoutVariables() {
        var key = unique("complete");
        engine.deploy(key, CibsevenRemoteTestProcesses.userTask(key, unique("user")));
        var processId = engine.start(key);
        var taskId = (String) engine.tasks(processId).getFirst().get("id");

        usecase.execute(
                MifosProcessCompleteUsecase.Request.builder().taskId(taskId).build());

        assertThat(engine.taskCount(taskId)).isZero();
    }
}
