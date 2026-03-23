/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifos.workflow.infrastructure.usecase.flowable.mapping;

import org.flowable.task.api.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mifos.boot.commons.mapping.MifosMapperConfiguration;
import org.mifos.workflow.infrastructure.core.model.MifosFlowTaskPendingResponse;

@Mapper(config = MifosMapperConfiguration.class)
public interface FlowableTaskPendingMapper {
    @Mapping(source = "id", target = "taskId")
    @Mapping(source = "processInstanceId", target = "processId")
    MifosFlowTaskPendingResponse map(Task task);
}
