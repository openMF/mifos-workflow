///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.usecase.flowableremote.implementation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mifos.workflow.process.core.usecase.MifosProcessStartUsecase;
import org.springframework.beans.factory.annotation.Autowired;

class FlowableRemoteStartUsecaseTest extends FlowableRemoteUsecaseTestSupport {
    @Autowired
    private MifosProcessStartUsecase usecase;

    @Test
    void startsProcessWithVariables() {
        var key = unique("start");
        engine.deploy(key, FlowableRemoteTestProcesses.userTask(key, unique("user")));

        var response = usecase.execute(MifosProcessStartUsecase.Request.builder()
                .key(key)
                .variables(Map.of("amount", 42, "approved", true, "note", "urgent"))
                .build());

        var instances = engine.processInstances(key);
        assertThat(instances).hasSize(1);
        var processId = (String) instances.getFirst().get("id");
        assertThat(response.getId()).isEqualTo(FlowableIds.toUuid(processId));
        assertThat(engine.variables(processId))
                .containsEntry("amount", 42)
                .containsEntry("approved", true)
                .containsEntry("note", "urgent");
    }

    @Test
    void startsProcessWithoutVariables() {
        var key = unique("start");
        engine.deploy(key, FlowableRemoteTestProcesses.userTask(key, unique("user")));

        var response = usecase.execute(
                MifosProcessStartUsecase.Request.builder().key(key).build());

        var instances = engine.processInstances(key);
        assertThat(instances).hasSize(1);
        var processId = (String) instances.getFirst().get("id");
        assertThat(response.getId()).isEqualTo(FlowableIds.toUuid(processId));
        assertThat(engine.variables(processId)).isEmpty();
    }
}
