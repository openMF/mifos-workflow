///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.usecase.flowableremote.implementation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.mifos.workflow.process.core.usecase.MifosProcessTaskPendingUsecase;
import org.springframework.beans.factory.annotation.Autowired;

class FlowableRemoteTaskPendingUsecaseTest extends FlowableRemoteUsecaseTestSupport {
    @Autowired
    private MifosProcessTaskPendingUsecase usecase;

    @Test
    void listsTasksAssignedToUser() {
        var key = unique("pending");
        var assignee = unique("user");
        engine.deploy(key, FlowableRemoteTestProcesses.userTask(key, assignee));
        var first = engine.start(key);
        var second = engine.start(key);

        var response = usecase.execute(MifosProcessTaskPendingUsecase.Request.builder()
                .userId(assignee)
                .build());

        assertThat(response.getTasks()).hasSize(2);
        assertThat(response.getTasks())
                .extracting(MifosProcessTaskPendingUsecase.Task::getProcessId)
                .containsExactlyInAnyOrder(first, second);
        assertThat(response.getTasks()).allSatisfy(task -> {
            assertThat(task.getTaskId()).isNotBlank();
            assertThat(task.getName()).isEqualTo("Review");
            assertThat(task.getAssignee()).isEqualTo(assignee);
            assertThat(task.getProcessDefinitionId()).isNotBlank();
            assertThat(task.getCreateTime()).isNotNull();
        });
    }

    @Test
    void excludesCompletedTasks() {
        var key = unique("pending");
        var assignee = unique("user");
        engine.deploy(key, FlowableRemoteTestProcesses.userTask(key, assignee));
        var processId = engine.start(key);
        engine.completeTask((String) engine.tasks(processId).getFirst().get("id"));

        var response = usecase.execute(MifosProcessTaskPendingUsecase.Request.builder()
                .userId(assignee)
                .build());

        assertThat(response.getTasks()).isEmpty();
    }
}
