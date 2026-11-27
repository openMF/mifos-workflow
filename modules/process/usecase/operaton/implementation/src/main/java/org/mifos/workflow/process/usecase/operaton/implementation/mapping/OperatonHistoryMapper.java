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
import org.mifos.workflow.process.core.usecase.MifosProcessHistoryUsecase;
import org.operaton.bpm.engine.history.HistoricProcessInstance;

@Mapper(config = MifosMapperConfiguration.class)
public interface OperatonHistoryMapper {
    @Mapping(source = "id", target = "processId")
    MifosProcessHistoryUsecase.MifosFlowHistoryEntry map(HistoricProcessInstance instance);

    List<MifosProcessHistoryUsecase.MifosFlowHistoryEntry> map(List<HistoricProcessInstance> instances);
}
