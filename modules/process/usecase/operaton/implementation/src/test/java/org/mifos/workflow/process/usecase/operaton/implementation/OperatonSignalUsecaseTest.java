///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.usecase.operaton.implementation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mifos.workflow.process.core.usecase.MifosProcessSignalUsecase;
import org.springframework.beans.factory.annotation.Autowired;

class OperatonSignalUsecaseTest extends OperatonUsecaseTestSupport {
    @Autowired
    private MifosProcessSignalUsecase usecase;

    @Test
    void signalsSingleProcess() {
        var key = unique("signal");
        var signal = unique("go");
        deploy(key, OperatonTestProcesses.signalCatch(key, signal));
        var target = start(key);
        var other = start(key);
        var targetId = OperatonIds.toUuid(target.getId());

        var response = usecase.execute(MifosProcessSignalUsecase.Request.builder()
                .id(targetId)
                .signalName(signal)
                .variables(Map.of("signalled", true))
                .build());

        assertThat(response.getId()).isEqualTo(targetId);
        assertThat(isRunning(target.getId())).isFalse();
        assertThat(isRunning(other.getId())).isTrue();
        assertThat(historicVariables(target.getId())).containsEntry("signalled", true);
    }

    @Test
    void broadcastsSignalToAllWaitingProcesses() {
        var key = unique("signal");
        var signal = unique("go");
        deploy(key, OperatonTestProcesses.signalCatch(key, signal));
        var first = start(key);
        var second = start(key);

        var response = usecase.execute(
                MifosProcessSignalUsecase.Request.builder().signalName(signal).build());

        assertThat(response.getId()).isNull();
        assertThat(isRunning(first.getId())).isFalse();
        assertThat(isRunning(second.getId())).isFalse();
    }

    private boolean isRunning(String processId) {
        return runtimeService
                        .createProcessInstanceQuery()
                        .processInstanceId(processId)
                        .count()
                > 0;
    }
}
