///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.usecase.flowableremote.implementation;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import lombok.experimental.UtilityClass;
import org.jspecify.annotations.Nullable;
import org.mifos.workflow.process.core.usecase.MifosProcessHistoryUsecase;
import org.mifos.workflow.process.core.usecase.MifosProcessTaskPendingUsecase;
import org.mifos.workflow.process.support.flowableremote.core.client.FlowableRemoteClient;

/// Maps REST API results to usecase responses.
///
/// Times are converted to the local time zone, like the embedded module converts `Date` to `LocalDateTime`.
@UtilityClass
class FlowableRemoteMapping {
    /// ISO 8601 with or without milliseconds, and with an offset as `Z`, `+01:00` or `+0100`.
    private static final DateTimeFormatter PARSE = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss[.SSS][XXX][XX]");

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    static List<MifosProcessTaskPendingUsecase.Task> toTasks(List<FlowableRemoteClient.Task> tasks) {
        return tasks.stream()
                .map(task -> MifosProcessTaskPendingUsecase.Task.builder()
                        .taskId(task.id())
                        .name(task.name())
                        .processId(task.processInstanceId())
                        .processDefinitionId(task.processDefinitionId())
                        .assignee(task.assignee())
                        .createTime(parse(task.createTime()))
                        .dueDate(parse(task.dueDate()))
                        .description(task.description())
                        .priority(task.priority())
                        .build())
                .toList();
    }

    /// @param definitionKeys finds the key of a process definition by its id, for responses without the key
    static List<MifosProcessHistoryUsecase.MifosFlowHistoryEntry> toEntries(
            List<FlowableRemoteClient.HistoricProcessInstance> instances, Function<String, String> definitionKeys) {
        return instances.stream()
                .map(instance -> MifosProcessHistoryUsecase.MifosFlowHistoryEntry.builder()
                        .processId(instance.id())
                        .processDefinitionId(instance.processDefinitionId())
                        .processDefinitionKey(processDefinitionKey(instance, definitionKeys))
                        .startTime(parse(instance.startTime()))
                        .endTime(parse(instance.endTime()))
                        .durationInMillis(instance.durationInMillis())
                        .startUserId(instance.startUserId())
                        .deleteReason(instance.deleteReason())
                        .build())
                .toList();
    }

    /// The REST API does not always send the key, and the process definition id does not contain it when the engine
    /// uses UUIDs, so the key is looked up by the id.
    static @Nullable String processDefinitionKey(
            FlowableRemoteClient.HistoricProcessInstance instance, Function<String, String> definitionKeys) {
        var key = instance.processDefinitionKey();
        var id = instance.processDefinitionId();
        if (key != null || id == null) {
            return key;
        }
        return definitionKeys.apply(id);
    }

    static @Nullable LocalDateTime parse(@Nullable String value) {
        if (value == null) {
            return null;
        }
        return OffsetDateTime.parse(value, PARSE)
                .atZoneSameInstant(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    static String format(Date value) {
        return FORMAT.format(value.toInstant().atZone(ZoneId.systemDefault()));
    }
}
