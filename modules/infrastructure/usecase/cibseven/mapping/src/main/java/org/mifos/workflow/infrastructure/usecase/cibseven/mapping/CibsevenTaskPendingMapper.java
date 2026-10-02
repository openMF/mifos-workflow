/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifos.workflow.infrastructure.usecase.cibseven.mapping;

import java.util.List;
import org.cibseven.bpm.engine.task.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mifos.boot.commons.mapping.MifosMapperConfiguration;
import org.mifos.workflow.infrastructure.core.model.MifosFlowTask;

@Mapper(config = MifosMapperConfiguration.class)
public interface CibsevenTaskPendingMapper {
    @Mapping(source = "id", target = "taskId")
    @Mapping(source = "processInstanceId", target = "processId")
    MifosFlowTask map(Task task);

    List<MifosFlowTask> map(List<Task> tasks);
}