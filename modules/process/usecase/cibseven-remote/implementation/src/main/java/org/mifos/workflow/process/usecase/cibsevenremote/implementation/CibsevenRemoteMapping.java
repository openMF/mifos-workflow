///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.usecase.cibsevenremote.implementation;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import lombok.experimental.UtilityClass;
import org.jspecify.annotations.Nullable;
import org.mifos.workflow.process.core.usecase.MifosProcessHistoryUsecase;
import org.mifos.workflow.process.core.usecase.MifosProcessTaskPendingUsecase;
import org.mifos.workflow.process.support.cibsevenremote.core.client.CibsevenRemoteClient;

/// Maps REST API results to usecase responses.
///
/// Times are converted to the local time zone, like the embedded module converts `Date` to `LocalDateTime`.
@UtilityClass
class CibsevenRemoteMapping {
    /// The default date format of the REST API, for example `2026-11-22T10:15:30.000+0100`.
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    static List<MifosProcessTaskPendingUsecase.Task> toTasks(List<CibsevenRemoteClient.Task> tasks) {
        return tasks.stream()
                .map(task -> MifosProcessTaskPendingUsecase.Task.builder()
                        .taskId(task.id())
                        .name(task.name())
                        .processId(task.processInstanceId())
                        .processDefinitionId(task.processDefinitionId())
                        .assignee(task.assignee())
                        .createTime(parse(task.created()))
                        .dueDate(parse(task.due()))
                        .description(task.description())
                        .priority(task.priority())
                        .build())
                .toList();
    }

    static List<MifosProcessHistoryUsecase.MifosFlowHistoryEntry> toEntries(
            List<CibsevenRemoteClient.HistoricProcessInstance> instances) {
        return instances.stream()
                .map(instance -> MifosProcessHistoryUsecase.MifosFlowHistoryEntry.builder()
                        .processId(instance.id())
                        .processDefinitionId(instance.processDefinitionId())
                        .processDefinitionKey(instance.processDefinitionKey())
                        .startTime(parse(instance.startTime()))
                        .endTime(parse(instance.endTime()))
                        .durationInMillis(instance.durationInMillis())
                        .startUserId(instance.startUserId())
                        .deleteReason(instance.deleteReason())
                        .build())
                .toList();
    }

    static @Nullable LocalDateTime parse(@Nullable String value) {
        if (value == null) {
            return null;
        }
        return OffsetDateTime.parse(value, FORMAT)
                .atZoneSameInstant(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    static String format(Date value) {
        return FORMAT.format(value.toInstant().atZone(ZoneId.systemDefault()));
    }
}
