///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.usecase.camunda.implementation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mifos.workflow.process.usecase.camunda.implementation.CamundaIds.toKey;

import io.camunda.client.api.command.ClientException;
import io.camunda.process.test.api.CamundaAssert;
import io.camunda.process.test.api.assertions.ProcessInstanceSelectors;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mifos.workflow.process.core.usecase.MifosProcessStartUsecase;
import org.springframework.beans.factory.annotation.Autowired;

final class CamundaStartUsecaseTest extends CamundaUsecaseTestSupport {
    @Autowired
    private MifosProcessStartUsecase usecase;

    @Test
    void startsProcessWithVariables() {
        var key = unique("start");
        deploy(key, CamundaTestProcesses.userTask(key, unique("user")));

        var response = usecase.execute(MifosProcessStartUsecase.Request.builder()
                .key(key)
                .variables(Map.of("amount", 42))
                .build());

        CamundaAssert.assertThat(ProcessInstanceSelectors.byKey(toKey(response.getId())))
                .isActive()
                .hasVariable("amount", 42);
    }

    @Test
    void startsProcessWithoutVariables() {
        var key = unique("start");
        deploy(key, CamundaTestProcesses.userTask(key, unique("user")));

        var response = usecase.execute(
                MifosProcessStartUsecase.Request.builder().key(key).build());

        assertThat(response.getId()).isNotNull();
        CamundaAssert.assertThat(ProcessInstanceSelectors.byKey(toKey(response.getId())))
                .isActive();
    }

    @Test
    void failsForUnknownProcess() {
        var request = MifosProcessStartUsecase.Request.builder()
                .key(unique("missing"))
                .build();

        assertThatThrownBy(() -> usecase.execute(request)).isInstanceOf(ClientException.class);
    }
}
