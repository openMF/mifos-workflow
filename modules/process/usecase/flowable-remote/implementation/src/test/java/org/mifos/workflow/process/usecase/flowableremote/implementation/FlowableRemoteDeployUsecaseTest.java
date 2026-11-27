///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.usecase.flowableremote.implementation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.mifos.workflow.process.core.usecase.MifosProcessDeployUsecase;
import org.springframework.beans.factory.annotation.Autowired;

class FlowableRemoteDeployUsecaseTest extends FlowableRemoteUsecaseTestSupport {
    @Autowired
    private MifosProcessDeployUsecase usecase;

    @Test
    void deploysProcessDefinition() {
        var key = unique("deploy");

        var response = usecase.execute(MifosProcessDeployUsecase.Request.builder()
                .name(key + ".bpmn")
                .processDefinition(FlowableRemoteTestProcesses.straightThrough(key))
                .build());

        // the deployment is found by its process definition: the REST API does not keep a name ending in ".bpmn"
        var definitions = engine.processDefinitions(key);
        assertThat(definitions).hasSize(1);
        var deploymentId = (String) definitions.getFirst().get("deploymentId");
        assertThat(response.getId()).isEqualTo(FlowableIds.toUuid(deploymentId));
        assertThat(engine.deploymentExists(deploymentId)).isTrue();
    }

    @Test
    void deploysProcessDefinitionWhenNameHasNoBpmnSuffix() {
        var key = unique("deploy");

        var response = usecase.execute(MifosProcessDeployUsecase.Request.builder()
                .name(key)
                .processDefinition(FlowableRemoteTestProcesses.straightThrough(key))
                .build());

        var definitions = engine.processDefinitions(key);
        assertThat(definitions).hasSize(1);
        var deploymentId = (String) definitions.getFirst().get("deploymentId");
        assertThat(response.getId()).isEqualTo(FlowableIds.toUuid(deploymentId));
        assertThat(engine.deploymentResourceNames(deploymentId)).containsExactly(key + ".bpmn");
    }
}
