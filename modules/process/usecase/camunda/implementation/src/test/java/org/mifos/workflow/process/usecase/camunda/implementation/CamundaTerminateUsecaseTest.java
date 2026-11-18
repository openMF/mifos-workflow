///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.usecase.camunda.implementation;

import static org.assertj.core.api.Assertions.assertThat;

import io.camunda.process.test.api.CamundaAssert;
import org.junit.jupiter.api.Test;
import org.mifos.workflow.process.core.usecase.MifosProcessTerminateUsecase;
import org.springframework.beans.factory.annotation.Autowired;

final class CamundaTerminateUsecaseTest extends CamundaUsecaseTestSupport {
    @Autowired
    private MifosProcessTerminateUsecase usecase;

    @Test
    void terminatesRunningProcessByUuid() {
        var key = unique("terminate");
        deploy(key, CamundaTestProcesses.userTask(key, unique("user")));
        var instance = start(key);

        var response = usecase.execute(MifosProcessTerminateUsecase.Request.builder()
                .processId(CamundaIds.toId(instance.getProcessInstanceKey()))
                .reason("no longer needed")
                .build());

        assertThat(response.getId()).isEqualTo(CamundaIds.toUuid(instance.getProcessInstanceKey()));
        CamundaAssert.assertThat(instance).isTerminated();
    }

    @Test
    void terminatesRunningProcessByRawKey() {
        var key = unique("terminate");
        deploy(key, CamundaTestProcesses.userTask(key, unique("user")));
        var instance = start(key);

        usecase.execute(MifosProcessTerminateUsecase.Request.builder()
                .processId(String.valueOf(instance.getProcessInstanceKey()))
                .build());

        CamundaAssert.assertThat(instance).isTerminated();
    }
}
