///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.usecase.cibseven.implementation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.mifos.workflow.process.core.usecase.MifosProcessDeleteUsecase;
import org.springframework.beans.factory.annotation.Autowired;

class CibsevenDeleteUsecaseTest extends CibsevenUsecaseTestSupport {
    @Autowired
    private MifosProcessDeleteUsecase usecase;

    @Test
    void deletesDeploymentIncludingRunningInstances() {
        var key = unique("delete");
        var deployment = deploy(key, CibsevenTestProcesses.userTask(key, unique("user")));
        var instance = start(key);

        var response = usecase.execute(MifosProcessDeleteUsecase.Request.builder()
                .deploymentId(deployment.getId())
                .build());

        assertThat(response.getId()).isEqualTo(CibsevenIds.toUuid(deployment.getId()));
        assertThat(repositoryService
                        .createDeploymentQuery()
                        .deploymentId(deployment.getId())
                        .count())
                .isZero();
        assertThat(repositoryService
                        .createProcessDefinitionQuery()
                        .processDefinitionKey(key)
                        .count())
                .isZero();
        assertThat(runtimeService
                        .createProcessInstanceQuery()
                        .processInstanceId(instance.getId())
                        .count())
                .isZero();
    }
}
