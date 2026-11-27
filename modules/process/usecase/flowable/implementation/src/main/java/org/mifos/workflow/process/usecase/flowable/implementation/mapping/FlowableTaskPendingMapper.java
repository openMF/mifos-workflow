///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.usecase.flowable.implementation.mapping;

import java.util.List;
import org.flowable.task.api.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mifos.commons.boot.mapping.MifosMapperConfiguration;
import org.mifos.workflow.process.core.usecase.MifosProcessTaskPendingUsecase;

/// Maps Flowable tasks to pending task entries.
@Mapper(config = MifosMapperConfiguration.class)
public interface FlowableTaskPendingMapper {
    @Mapping(source = "id", target = "taskId")
    @Mapping(source = "processInstanceId", target = "processId")
    MifosProcessTaskPendingUsecase.Task map(Task task);

    List<MifosProcessTaskPendingUsecase.Task> map(List<Task> tasks);
}
