///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.usecase.camunda.implementation;

import static org.assertj.core.api.Assertions.assertThat;

import io.camunda.process.test.api.CamundaAssert;
import org.junit.jupiter.api.Test;
import org.mifos.workflow.process.core.usecase.MifosProcessHistoryUsecase;
import org.springframework.beans.factory.annotation.Autowired;

final class CamundaHistoryUsecaseTest extends CamundaUsecaseTestSupport {
    @Autowired
    private MifosProcessHistoryUsecase usecase;

    @Test
    void returnsFinishedProcessById() {
        var key = unique("history");
        deploy(key, CamundaTestProcesses.straightThrough(key));
        var instance = start(key);
        var request = MifosProcessHistoryUsecase.Request.builder()
                .id(CamundaIds.toUuid(instance.getProcessInstanceKey()))
                .build();

        eventually(() -> assertThat(usecase.execute(request).getEntries())
                .singleElement()
                .satisfies(entry -> {
                    assertThat(entry.getProcessId()).isEqualTo(CamundaIds.toId(instance.getProcessInstanceKey()));
                    assertThat(entry.getProcessDefinitionId())
                            .isEqualTo(CamundaIds.toId(instance.getProcessDefinitionKey()));
                    assertThat(entry.getProcessDefinitionKey()).isEqualTo(key);
                    assertThat(entry.getStartTime()).isNotNull();
                    assertThat(entry.getEndTime()).isNotNull();
                    assertThat(entry.getDurationInMillis()).isNotNull().isNotNegative();
                }));
    }

    @Test
    void returnsAllFinishedProcessesWithoutId() {
        var key = unique("history");
        deploy(key, CamundaTestProcesses.straightThrough(key));
        var first = start(key);
        var second = start(key);
        var request = MifosProcessHistoryUsecase.Request.builder().build();

        eventually(() -> assertThat(usecase.execute(request).getEntries())
                .extracting(MifosProcessHistoryUsecase.MifosFlowHistoryEntry::getProcessId)
                .contains(
                        CamundaIds.toId(first.getProcessInstanceKey()),
                        CamundaIds.toId(second.getProcessInstanceKey())));
    }

    @Test
    void excludesRunningProcesses() {
        var key = unique("history");
        deploy(key, CamundaTestProcesses.userTask(key, unique("user")));
        var instance = start(key);
        // once the instance is visible as waiting at its task, the search API has caught up with it
        CamundaAssert.assertThat(instance).hasActiveElements("review");

        var response = usecase.execute(MifosProcessHistoryUsecase.Request.builder()
                .id(CamundaIds.toUuid(instance.getProcessInstanceKey()))
                .build());

        assertThat(response.getEntries()).isEmpty();
    }
}
