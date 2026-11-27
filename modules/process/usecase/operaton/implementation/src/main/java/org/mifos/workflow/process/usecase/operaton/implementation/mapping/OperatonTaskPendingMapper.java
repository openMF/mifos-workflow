///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.usecase.operaton.implementation.mapping;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mifos.commons.boot.mapping.MifosMapperConfiguration;
import org.mifos.workflow.process.core.usecase.MifosProcessTaskPendingUsecase;
import org.operaton.bpm.engine.task.Task;

@Mapper(config = MifosMapperConfiguration.class)
public interface OperatonTaskPendingMapper {
    @Mapping(source = "id", target = MifosProcessTaskPendingUsecase.Task.Fields.taskId)
    @Mapping(source = "processInstanceId", target = MifosProcessTaskPendingUsecase.Task.Fields.processId)
    MifosProcessTaskPendingUsecase.Task map(Task task);

    List<MifosProcessTaskPendingUsecase.Task> map(List<Task> tasks);
}
