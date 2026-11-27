///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.usecase.camunda.implementation;

import static org.assertj.core.api.Assertions.assertThat;

import io.camunda.process.test.api.CamundaAssert;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mifos.workflow.process.core.usecase.MifosProcessCompleteUsecase;
import org.springframework.beans.factory.annotation.Autowired;

final class CamundaCompleteUsecaseTest extends CamundaUsecaseTestSupport {
    @Autowired
    private MifosProcessCompleteUsecase usecase;

    @Test
    void completesTaskAndFinishesProcess() {
        var key = unique("complete");
        deploy(key, CamundaTestProcesses.userTask(key, unique("user")));
        var instance = start(key);
        var taskKey = userTaskKey(instance.getProcessInstanceKey());

        var response = usecase.execute(MifosProcessCompleteUsecase.Request.builder()
                .taskId(String.valueOf(taskKey))
                .variables(Map.of("approved", true))
                .build());

        assertThat(response.getId()).isEqualTo(CamundaIds.toUuid(taskKey));
        CamundaAssert.assertThat(instance).isCompleted().hasVariable("approved", true);
    }

    @Test
    void completesTaskWithoutVariables() {
        var key = unique("complete");
        deploy(key, CamundaTestProcesses.userTask(key, unique("user")));
        var instance = start(key);
        var taskKey = userTaskKey(instance.getProcessInstanceKey());

        usecase.execute(MifosProcessCompleteUsecase.Request.builder()
                .taskId(String.valueOf(taskKey))
                .build());

        CamundaAssert.assertThat(instance).isCompleted();
    }

    @Test
    void completesTaskByIdFromPendingTasks() {
        var key = unique("complete");
        deploy(key, CamundaTestProcesses.userTask(key, unique("user")));
        var instance = start(key);
        var taskKey = userTaskKey(instance.getProcessInstanceKey());

        // the pending task usecase hands out UUID-form ids
        usecase.execute(MifosProcessCompleteUsecase.Request.builder()
                .taskId(CamundaIds.toId(taskKey))
                .build());

        CamundaAssert.assertThat(instance).isCompleted();
    }
}
