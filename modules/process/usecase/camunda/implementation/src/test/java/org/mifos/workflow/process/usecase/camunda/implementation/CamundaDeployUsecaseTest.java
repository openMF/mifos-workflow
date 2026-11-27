///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.usecase.camunda.implementation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.mifos.workflow.process.core.usecase.MifosProcessDeployUsecase;
import org.springframework.beans.factory.annotation.Autowired;

final class CamundaDeployUsecaseTest extends CamundaUsecaseTestSupport {
    @Autowired
    private MifosProcessDeployUsecase usecase;

    @Test
    void deploysProcessDefinition() {
        var key = unique("deploy");

        var response = usecase.execute(MifosProcessDeployUsecase.Request.builder()
                .name(key + ".bpmn")
                .processDefinition(CamundaTestProcesses.straightThrough(key))
                .build());

        var instance = start(key);
        assertThat(response.getId()).isEqualTo(CamundaIds.toUuid(instance.getProcessDefinitionKey()));
    }

    @Test
    void deploysProcessDefinitionWhenNameHasNoBpmnSuffix() {
        var key = unique("deploy");

        var response = usecase.execute(MifosProcessDeployUsecase.Request.builder()
                .name(key)
                .processDefinition(CamundaTestProcesses.straightThrough(key))
                .build());

        var instance = start(key);
        assertThat(response.getId()).isEqualTo(CamundaIds.toUuid(instance.getProcessDefinitionKey()));
    }
}
