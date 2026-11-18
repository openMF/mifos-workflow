///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.usecase.eximee.implementation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.mifos.workflow.process.core.usecase.MifosProcessDeployUsecase;
import org.springframework.beans.factory.annotation.Autowired;

class EximeeDeployUsecaseTest extends EximeeUsecaseTestSupport {
    @Autowired
    private MifosProcessDeployUsecase usecase;

    @Test
    void deploysProcessDefinition() {
        var key = unique("deploy");

        var response = usecase.execute(MifosProcessDeployUsecase.Request.builder()
                .name(key + ".bpmn")
                .processDefinition(EximeeTestProcesses.straightThrough(key))
                .build());

        var deployment = repositoryService
                .createDeploymentQuery()
                .deploymentName(key + ".bpmn")
                .singleResult();
        assertThat(deployment).isNotNull();
        assertThat(response.getId()).isEqualTo(EximeeIds.toUuid(deployment.getId()));

        var definition = repositoryService
                .createProcessDefinitionQuery()
                .processDefinitionKey(key)
                .singleResult();
        assertThat(definition).isNotNull();
        assertThat(definition.getDeploymentId()).isEqualTo(deployment.getId());
    }

    @Test
    void deploysProcessDefinitionWhenNameHasNoBpmnSuffix() {
        var key = unique("deploy");

        var response = usecase.execute(MifosProcessDeployUsecase.Request.builder()
                .name(key)
                .processDefinition(EximeeTestProcesses.straightThrough(key))
                .build());

        var deployment =
                repositoryService.createDeploymentQuery().deploymentName(key).singleResult();
        assertThat(deployment).isNotNull();
        assertThat(response.getId()).isEqualTo(EximeeIds.toUuid(deployment.getId()));
        assertThat(repositoryService.getDeploymentResourceNames(deployment.getId()))
                .containsExactly(key + ".bpmn");
        assertThat(repositoryService
                        .createProcessDefinitionQuery()
                        .deploymentId(deployment.getId())
                        .processDefinitionKey(key)
                        .count())
                .isEqualTo(1);
    }
}
