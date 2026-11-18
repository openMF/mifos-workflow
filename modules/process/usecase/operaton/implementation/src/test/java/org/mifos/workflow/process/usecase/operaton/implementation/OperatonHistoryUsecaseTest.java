///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.usecase.operaton.implementation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.mifos.workflow.process.core.usecase.MifosProcessHistoryUsecase;
import org.springframework.beans.factory.annotation.Autowired;

class OperatonHistoryUsecaseTest extends OperatonUsecaseTestSupport {
    @Autowired
    private MifosProcessHistoryUsecase usecase;

    @Test
    void returnsFinishedProcessById() {
        var key = unique("history");
        deploy(key, OperatonTestProcesses.straightThrough(key));
        var instance = start(key);

        var response = usecase.execute(MifosProcessHistoryUsecase.Request.builder()
                .id(OperatonIds.toUuid(instance.getId()))
                .build());

        assertThat(response.getEntries()).singleElement().satisfies(entry -> {
            assertThat(entry.getProcessId()).isEqualTo(instance.getId());
            assertThat(entry.getProcessDefinitionId()).isEqualTo(instance.getProcessDefinitionId());
            assertThat(entry.getProcessDefinitionKey()).isEqualTo(key);
            assertThat(entry.getStartTime()).isNotNull();
            assertThat(entry.getEndTime()).isNotNull();
        });
    }

    @Test
    void returnsAllFinishedProcessesWithoutId() {
        var key = unique("history");
        deploy(key, OperatonTestProcesses.straightThrough(key));
        var first = start(key);
        var second = start(key);

        var response =
                usecase.execute(MifosProcessHistoryUsecase.Request.builder().build());

        assertThat(response.getEntries())
                .extracting(MifosProcessHistoryUsecase.MifosFlowHistoryEntry::getProcessId)
                .contains(first.getId(), second.getId());
    }

    @Test
    void excludesRunningProcesses() {
        var key = unique("history");
        deploy(key, OperatonTestProcesses.userTask(key, unique("user")));
        var instance = start(key);

        var response = usecase.execute(MifosProcessHistoryUsecase.Request.builder()
                .id(OperatonIds.toUuid(instance.getId()))
                .build());

        assertThat(response.getEntries()).isEmpty();
    }
}
