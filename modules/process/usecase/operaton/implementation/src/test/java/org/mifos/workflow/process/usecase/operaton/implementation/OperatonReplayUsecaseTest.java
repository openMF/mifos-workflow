///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.usecase.operaton.implementation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mifos.workflow.process.core.usecase.MifosProcessReplayUsecase;
import org.springframework.beans.factory.annotation.Autowired;

class OperatonReplayUsecaseTest extends OperatonUsecaseTestSupport {
    @Autowired
    private MifosProcessReplayUsecase usecase;

    @Test
    void replaysProcessWithMergedVariables() {
        var key = unique("replay");
        deploy(key, OperatonTestProcesses.straightThrough(key));
        var source = start(key, Map.of("a", 1, "b", 2));

        var response = usecase.execute(MifosProcessReplayUsecase.Request.builder()
                .sourceProcessId(OperatonIds.toUuid(source.getId()))
                .variables(Map.of("b", 3))
                .build());

        var replayed = historyService.createHistoricProcessInstanceQuery().processDefinitionKey(key).list().stream()
                .filter(instance -> !instance.getId().equals(source.getId()))
                .toList();
        assertThat(replayed).singleElement().satisfies(instance -> {
            assertThat(response.getId()).isEqualTo(OperatonIds.toUuid(instance.getId()));
            assertThat(historicVariables(instance.getId()))
                    .containsEntry("a", 1)
                    .containsEntry("b", 3);
        });
    }

    @Test
    void replaysProcessWithSourceVariablesOnly() {
        var key = unique("replay");
        deploy(key, OperatonTestProcesses.straightThrough(key));
        var source = start(key, Map.of("a", 1));

        var response = usecase.execute(MifosProcessReplayUsecase.Request.builder()
                .sourceProcessId(OperatonIds.toUuid(source.getId()))
                .build());

        assertThat(response.getId()).isNotNull().isNotEqualTo(OperatonIds.toUuid(source.getId()));
        assertThat(historyService
                        .createHistoricProcessInstanceQuery()
                        .processDefinitionKey(key)
                        .count())
                .isEqualTo(2);
    }
}
