///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.usecase.camunda.implementation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.camunda.client.api.command.ClientException;
import io.camunda.process.test.api.CamundaAssert;
import org.junit.jupiter.api.Test;
import org.mifos.workflow.process.core.usecase.MifosProcessDeleteUsecase;
import org.mifos.workflow.process.core.usecase.MifosProcessDeployUsecase;
import org.springframework.beans.factory.annotation.Autowired;

final class CamundaDeleteUsecaseTest extends CamundaUsecaseTestSupport {
    @Autowired
    private MifosProcessDeployUsecase deployUsecase;

    @Autowired
    private MifosProcessDeleteUsecase usecase;

    @Test
    void deletesDeploymentIncludingRunningInstances() {
        var key = unique("delete");
        var deployment = deployUsecase.execute(MifosProcessDeployUsecase.Request.builder()
                .name(key)
                .processDefinition(CamundaTestProcesses.userTask(key, unique("user")))
                .build());
        var instance = start(key);
        CamundaAssert.assertThat(instance).isActive();

        var response = usecase.execute(MifosProcessDeleteUsecase.Request.builder()
                .deploymentId(deployment.getId().toString())
                .build());

        assertThat(response.getId()).isEqualTo(deployment.getId());
        CamundaAssert.assertThat(instance).isTerminated();
        assertThatThrownBy(() -> start(key)).isInstanceOf(ClientException.class);
    }
}
