///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.usecase.flowableremote.implementation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mifos.workflow.process.core.usecase.MifosProcessReplayUsecase;
import org.springframework.beans.factory.annotation.Autowired;

class FlowableRemoteReplayUsecaseTest extends FlowableRemoteUsecaseTestSupport {
    @Autowired
    private MifosProcessReplayUsecase usecase;

    @Test
    void replaysProcessWithMergedVariables() {
        var key = unique("replay");
        engine.deploy(key, FlowableRemoteTestProcesses.straightThrough(key));
        var source = engine.start(key, Map.of("a", 1, "b", 2));

        var response = usecase.execute(MifosProcessReplayUsecase.Request.builder()
                .sourceProcessId(FlowableIds.toUuid(source))
                .variables(Map.of("b", 3))
                .build());

        var replayed = engine.historicProcessInstances(key).stream()
                .map(instance -> (String) instance.get("id"))
                .filter(id -> !id.equals(source))
                .toList();
        assertThat(replayed).singleElement().satisfies(id -> {
            assertThat(response.getId()).isEqualTo(FlowableIds.toUuid(id));
            assertThat(engine.historicVariables(id)).containsEntry("a", 1).containsEntry("b", 3);
        });
    }

    @Test
    void replaysProcessWithSourceVariablesOnly() {
        var key = unique("replay");
        engine.deploy(key, FlowableRemoteTestProcesses.straightThrough(key));
        var source = engine.start(key, Map.of("a", 1));

        var response = usecase.execute(MifosProcessReplayUsecase.Request.builder()
                .sourceProcessId(FlowableIds.toUuid(source))
                .build());

        assertThat(response.getId()).isNotNull().isNotEqualTo(FlowableIds.toUuid(source));
        assertThat(engine.historicProcessInstances(key)).hasSize(2);
    }
}
