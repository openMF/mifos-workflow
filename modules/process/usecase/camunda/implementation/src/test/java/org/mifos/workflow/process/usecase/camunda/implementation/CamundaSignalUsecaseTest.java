///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.usecase.camunda.implementation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.camunda.process.test.api.CamundaAssert;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mifos.workflow.process.core.usecase.MifosProcessSignalUsecase;
import org.springframework.beans.factory.annotation.Autowired;

final class CamundaSignalUsecaseTest extends CamundaUsecaseTestSupport {
    @Autowired
    private MifosProcessSignalUsecase usecase;

    @Test
    void broadcastsSignalToAllWaitingProcesses() {
        var key = unique("signal");
        var signal = unique("go");
        deploy(key, CamundaTestProcesses.signalCatch(key, signal));
        var first = start(key);
        var second = start(key);
        // the signal subscription only exists once the instance has reached the catch event
        CamundaAssert.assertThat(first).hasActiveElements("wait");
        CamundaAssert.assertThat(second).hasActiveElements("wait");

        var response = usecase.execute(MifosProcessSignalUsecase.Request.builder()
                .signalName(signal)
                .variables(Map.of("signalled", true))
                .build());

        assertThat(response.getId()).isNull();
        CamundaAssert.assertThat(first).isCompleted().hasVariable("signalled", true);
        CamundaAssert.assertThat(second).isCompleted().hasVariable("signalled", true);
    }

    @Test
    void rejectsSignalForSingleProcess() {
        var key = unique("signal");
        var signal = unique("go");
        deploy(key, CamundaTestProcesses.signalCatch(key, signal));
        var instance = start(key);
        var request = MifosProcessSignalUsecase.Request.builder()
                .id(CamundaIds.toUuid(instance.getProcessInstanceKey()))
                .signalName(signal)
                .build();

        assertThatThrownBy(() -> usecase.execute(request))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining(signal);
        CamundaAssert.assertThat(instance).isActive();
    }
}
