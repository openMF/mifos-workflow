///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.usecase.camunda.implementation;

import static org.mifos.workflow.process.core.MifosProcessConstants.MIFOS_WORKFLOW_PROCESS_PROPERTIES_ENGINE;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.search.enums.UserTaskState;
import io.camunda.client.api.search.response.UserTask;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.mifos.workflow.process.core.usecase.MifosProcessTaskPendingUsecase;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/// Lists open user tasks from Camunda's search API, which trails the broker slightly: a task shows up shortly after
/// it is created, not in the same instant.
@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnProperty(name = MIFOS_WORKFLOW_PROCESS_PROPERTIES_ENGINE, havingValue = "CAMUNDA")
final class CamundaTaskPendingUsecase implements MifosProcessTaskPendingUsecase {
    private final CamundaClient client;

    @Override
    public Response execute(Request request) {
        var tasks = client.newUserTaskSearchRequest()
                .filter(filter -> {
                    filter.state(UserTaskState.CREATED);
                    if (request.getUserId() != null) {
                        filter.assignee(request.getUserId());
                    }
                })
                .send()
                .join()
                .items();

        log.debug("found {} pending tasks for {}", tasks.size(), request.getUserId());

        return Response.builder()
                .tasks(tasks.stream().map(CamundaTaskPendingUsecase::map).toList())
                .build();
    }

    private static Task map(UserTask task) {
        return Task.builder()
                .taskId(CamundaIds.toId(task.getUserTaskKey()))
                .name(task.getName())
                .processId(CamundaIds.toId(task.getProcessInstanceKey()))
                .processDefinitionId(CamundaIds.toId(task.getProcessDefinitionKey()))
                .assignee(task.getAssignee())
                .createTime(toLocalDateTime(task.getCreationDate()))
                .dueDate(toLocalDateTime(task.getDueDate()))
                .priority(task.getPriority())
                .build();
    }

    static @Nullable LocalDateTime toLocalDateTime(@Nullable OffsetDateTime value) {
        return value == null
                ? null
                : value.atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
    }
}
