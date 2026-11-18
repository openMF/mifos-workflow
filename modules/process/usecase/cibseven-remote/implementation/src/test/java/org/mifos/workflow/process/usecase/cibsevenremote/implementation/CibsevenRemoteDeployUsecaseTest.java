///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.usecase.cibsevenremote.implementation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.mifos.workflow.process.core.usecase.MifosProcessDeployUsecase;
import org.springframework.beans.factory.annotation.Autowired;

class CibsevenRemoteDeployUsecaseTest extends CibsevenRemoteUsecaseTestSupport {
    @Autowired
    private MifosProcessDeployUsecase usecase;

    @Test
    void deploysProcessDefinition() {
        var key = unique("deploy");

        var response = usecase.execute(MifosProcessDeployUsecase.Request.builder()
                .name(key + ".bpmn")
                .processDefinition(CibsevenRemoteTestProcesses.straightThrough(key))
                .build());

        var deployments = engine.deployments(key + ".bpmn");
        assertThat(deployments).hasSize(1);
        var deploymentId = (String) deployments.getFirst().get("id");
        assertThat(response.getId()).isEqualTo(CibsevenIds.toUuid(deploymentId));
        assertThat(engine.processDefinitions(key))
                .singleElement()
                .satisfies(
                        definition -> assertThat(definition.get("deploymentId")).isEqualTo(deploymentId));
    }

    @Test
    void deploysProcessDefinitionWhenNameHasNoBpmnSuffix() {
        var key = unique("deploy");

        var response = usecase.execute(MifosProcessDeployUsecase.Request.builder()
                .name(key)
                .processDefinition(CibsevenRemoteTestProcesses.straightThrough(key))
                .build());

        var deployments = engine.deployments(key);
        assertThat(deployments).hasSize(1);
        var deploymentId = (String) deployments.getFirst().get("id");
        assertThat(response.getId()).isEqualTo(CibsevenIds.toUuid(deploymentId));
        assertThat(engine.deploymentResourceNames(deploymentId)).containsExactly(key + ".bpmn");
        assertThat(engine.processDefinitions(key, deploymentId)).hasSize(1);
    }
}
