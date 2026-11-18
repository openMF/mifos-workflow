///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.usecase.eximee.implementation.mapping;

import java.util.List;
import org.eximeebpms.bpm.engine.history.HistoricProcessInstance;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mifos.commons.boot.mapping.MifosMapperConfiguration;
import org.mifos.workflow.process.core.usecase.MifosProcessHistoryUsecase;

/// Maps Eximee historic process instances to process history entries.
@Mapper(config = MifosMapperConfiguration.class)
public interface EximeeHistoryMapper {
    @Mapping(source = "id", target = "processId")
    MifosProcessHistoryUsecase.MifosFlowHistoryEntry map(HistoricProcessInstance instance);

    List<MifosProcessHistoryUsecase.MifosFlowHistoryEntry> map(List<HistoricProcessInstance> instances);
}
