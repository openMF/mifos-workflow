///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.usecase.cibsevenremote.implementation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mifos.workflow.process.core.usecase.MifosProcessSignalUsecase;
import org.springframework.beans.factory.annotation.Autowired;

class CibsevenRemoteSignalUsecaseTest extends CibsevenRemoteUsecaseTestSupport {
    @Autowired
    private MifosProcessSignalUsecase usecase;

    @Test
    void signalsSingleProcess() {
        var key = unique("signal");
        var signal = unique("go");
        engine.deploy(key, CibsevenRemoteTestProcesses.signalCatch(key, signal));
        var target = engine.start(key);
        var other = engine.start(key);
        var targetId = CibsevenIds.toUuid(target);

        var response = usecase.execute(MifosProcessSignalUsecase.Request.builder()
                .id(targetId)
                .signalName(signal)
                .variables(Map.of("signalled", true))
                .build());

        assertThat(response.getId()).isEqualTo(targetId);
        assertThat(engine.isRunning(target)).isFalse();
        assertThat(engine.isRunning(other)).isTrue();
        assertThat(engine.historicVariables(target)).containsEntry("signalled", true);
    }

    @Test
    void broadcastsSignalToAllWaitingProcesses() {
        var key = unique("signal");
        var signal = unique("go");
        engine.deploy(key, CibsevenRemoteTestProcesses.signalCatch(key, signal));
        var first = engine.start(key);
        var second = engine.start(key);

        var response = usecase.execute(
                MifosProcessSignalUsecase.Request.builder().signalName(signal).build());

        assertThat(response.getId()).isNull();
        assertThat(engine.isRunning(first)).isFalse();
        assertThat(engine.isRunning(second)).isFalse();
    }

    @Test
    void rejectsProcessThatDoesNotWaitForSignal() {
        var request = MifosProcessSignalUsecase.Request.builder()
                .id(UUID.randomUUID())
                .signalName(unique("go"))
                .build();

        assertThatThrownBy(() -> usecase.execute(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("found 0");
    }
}
