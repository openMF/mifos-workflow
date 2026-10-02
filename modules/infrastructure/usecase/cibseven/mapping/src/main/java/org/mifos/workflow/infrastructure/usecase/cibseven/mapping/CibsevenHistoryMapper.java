/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifos.workflow.infrastructure.usecase.cibseven.mapping;

import java.util.List;
import org.cibseven.bpm.engine.history.HistoricProcessInstance;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mifos.boot.commons.mapping.MifosMapperConfiguration;
import org.mifos.workflow.infrastructure.core.model.MifosFlowHistoryEntry;

@Mapper(config = MifosMapperConfiguration.class)
public interface CibsevenHistoryMapper {
    @Mapping(source = "id", target = "processId")
    MifosFlowHistoryEntry map(HistoricProcessInstance instance);

    List<MifosFlowHistoryEntry> map(List<HistoricProcessInstance> instances);
}