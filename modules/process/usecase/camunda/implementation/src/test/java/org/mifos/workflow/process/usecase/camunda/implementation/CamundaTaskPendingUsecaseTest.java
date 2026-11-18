///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.usecase.camunda.implementation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mifos.workflow.process.core.usecase.MifosProcessTaskPendingUsecase;
import org.springframework.beans.factory.annotation.Autowired;

final class CamundaTaskPendingUsecaseTest extends CamundaUsecaseTestSupport {
    @Autowired
    private MifosProcessTaskPendingUsecase usecase;

    @Test
    void listsTasksAssignedToUser() {
        var key = unique("pending");
        var assignee = unique("user");
        deploy(key, CamundaTestProcesses.userTask(key, assignee));
        var first = start(key);
        var second = start(key);
        var request = MifosProcessTaskPendingUsecase.Request.builder()
                .userId(assignee)
                .build();

        eventually(() -> {
            var response = usecase.execute(request);

            assertThat(response.getTasks())
                    .extracting(MifosProcessTaskPendingUsecase.Task::getProcessId)
                    .containsExactlyInAnyOrder(
                            CamundaIds.toId(first.getProcessInstanceKey()),
                            CamundaIds.toId(second.getProcessInstanceKey()));
            assertThat(response.getTasks()).allSatisfy(task -> {
                assertThat(task.getTaskId()).isNotBlank();
                assertThat(task.getName()).isEqualTo("Review");
                assertThat(task.getAssignee()).isEqualTo(assignee);
                assertThat(task.getProcessDefinitionId()).isEqualTo(CamundaIds.toId(first.getProcessDefinitionKey()));
                assertThat(task.getCreateTime()).isNotNull();
            });
        });
    }

    @Test
    void excludesCompletedTasks() {
        var key = unique("pending");
        var assignee = unique("user");
        deploy(key, CamundaTestProcesses.userTask(key, assignee));
        var instance = start(key);
        var taskKey = userTaskKey(instance.getProcessInstanceKey());
        client.newCompleteUserTaskCommand(taskKey).variables(Map.of()).send().join();
        var request = MifosProcessTaskPendingUsecase.Request.builder()
                .userId(assignee)
                .build();

        // the task was visible before (userTaskKey waited for it), so an empty list means it was filtered out
        eventually(() -> assertThat(usecase.execute(request).getTasks()).isEmpty());
    }
}
