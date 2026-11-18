///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.usecase.eximee.implementation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.mifos.workflow.process.core.usecase.MifosProcessTaskPendingUsecase;
import org.springframework.beans.factory.annotation.Autowired;

class EximeeTaskPendingUsecaseTest extends EximeeUsecaseTestSupport {
    @Autowired
    private MifosProcessTaskPendingUsecase usecase;

    @Test
    void listsTasksAssignedToUser() {
        var key = unique("pending");
        var assignee = unique("user");
        deploy(key, EximeeTestProcesses.userTask(key, assignee));
        var first = start(key);
        var second = start(key);

        var response = usecase.execute(MifosProcessTaskPendingUsecase.Request.builder()
                .userId(assignee)
                .build());

        assertThat(response.getTasks()).hasSize(2);
        assertThat(response.getTasks())
                .extracting(MifosProcessTaskPendingUsecase.Task::getProcessId)
                .containsExactlyInAnyOrder(first.getId(), second.getId());
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
        deploy(key, EximeeTestProcesses.userTask(key, assignee));
        var instance = start(key);
        taskService.complete(taskService
                .createTaskQuery()
                .processInstanceId(instance.getId())
                .singleResult()
                .getId());

        var response = usecase.execute(MifosProcessTaskPendingUsecase.Request.builder()
                .userId(assignee)
                .build());

        assertThat(response.getTasks()).isEmpty();
    }
}
