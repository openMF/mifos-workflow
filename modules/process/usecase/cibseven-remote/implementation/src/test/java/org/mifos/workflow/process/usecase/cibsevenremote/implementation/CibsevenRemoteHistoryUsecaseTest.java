///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.usecase.cibsevenremote.implementation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.mifos.workflow.process.core.usecase.MifosProcessHistoryUsecase;
import org.springframework.beans.factory.annotation.Autowired;

class CibsevenRemoteHistoryUsecaseTest extends CibsevenRemoteUsecaseTestSupport {
    @Autowired
    private MifosProcessHistoryUsecase usecase;

    @Test
    void returnsFinishedProcessById() {
        var key = unique("history");
        engine.deploy(key, CibsevenRemoteTestProcesses.straightThrough(key));
        var processId = engine.start(key);
        var definitionId = engine.historicProcessInstance(processId).get("processDefinitionId");

        var response = usecase.execute(MifosProcessHistoryUsecase.Request.builder()
                .id(CibsevenIds.toUuid(processId))
                .build());

        assertThat(response.getEntries()).singleElement().satisfies(entry -> {
            assertThat(entry.getProcessId()).isEqualTo(processId);
            assertThat(entry.getProcessDefinitionId()).isEqualTo(definitionId);
            assertThat(entry.getProcessDefinitionKey()).isEqualTo(key);
            assertThat(entry.getStartTime()).isNotNull();
            assertThat(entry.getEndTime()).isNotNull();
            assertThat(entry.getEndTime()).isAfterOrEqualTo(entry.getStartTime());
        });
    }

    @Test
    void returnsAllFinishedProcessesWithoutId() {
        var key = unique("history");
        engine.deploy(key, CibsevenRemoteTestProcesses.straightThrough(key));
        var first = engine.start(key);
        var second = engine.start(key);

        var response =
                usecase.execute(MifosProcessHistoryUsecase.Request.builder().build());

        assertThat(response.getEntries())
                .extracting(MifosProcessHistoryUsecase.MifosFlowHistoryEntry::getProcessId)
                .contains(first, second);
    }

    @Test
    void excludesRunningProcesses() {
        var key = unique("history");
        engine.deploy(key, CibsevenRemoteTestProcesses.userTask(key, unique("user")));
        var processId = engine.start(key);

        var response = usecase.execute(MifosProcessHistoryUsecase.Request.builder()
                .id(CibsevenIds.toUuid(processId))
                .build());

        assertThat(response.getEntries()).isEmpty();
    }
}
