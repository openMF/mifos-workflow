///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.usecase.flowableremote.implementation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.mifos.workflow.process.core.usecase.MifosProcessDeleteUsecase;
import org.springframework.beans.factory.annotation.Autowired;

class FlowableRemoteDeleteUsecaseTest extends FlowableRemoteUsecaseTestSupport {
    @Autowired
    private MifosProcessDeleteUsecase usecase;

    @Test
    void deletesDeploymentIncludingRunningInstances() {
        var key = unique("delete");
        var deploymentId = engine.deploy(key, FlowableRemoteTestProcesses.userTask(key, unique("user")));
        var processId = engine.start(key);

        var response = usecase.execute(MifosProcessDeleteUsecase.Request.builder()
                .deploymentId(deploymentId)
                .build());

        assertThat(response.getId()).isEqualTo(FlowableIds.toUuid(deploymentId));
        assertThat(engine.deploymentExists(deploymentId)).isFalse();
        assertThat(engine.processDefinitions(key)).isEmpty();
        assertThat(engine.isRunning(processId)).isFalse();
    }
}
