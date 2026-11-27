///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.usecase.eximee.implementation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mifos.workflow.process.core.usecase.MifosProcessStartUsecase;
import org.springframework.beans.factory.annotation.Autowired;

class EximeeStartUsecaseTest extends EximeeUsecaseTestSupport {
    @Autowired
    private MifosProcessStartUsecase usecase;

    @Test
    void startsProcessWithVariables() {
        var key = unique("start");
        deploy(key, EximeeTestProcesses.userTask(key, unique("user")));

        var response = usecase.execute(MifosProcessStartUsecase.Request.builder()
                .key(key)
                .variables(Map.of("amount", 42))
                .build());

        var instance = runtimeService
                .createProcessInstanceQuery()
                .processDefinitionKey(key)
                .singleResult();
        assertThat(instance).isNotNull();
        assertThat(response.getId()).isEqualTo(EximeeIds.toUuid(instance.getId()));
        assertThat(runtimeService.getVariables(instance.getId())).containsEntry("amount", 42);
    }

    @Test
    void startsProcessWithoutVariables() {
        var key = unique("start");
        deploy(key, EximeeTestProcesses.userTask(key, unique("user")));

        var response = usecase.execute(
                MifosProcessStartUsecase.Request.builder().key(key).build());

        var instance = runtimeService
                .createProcessInstanceQuery()
                .processDefinitionKey(key)
                .singleResult();
        assertThat(instance).isNotNull();
        assertThat(response.getId()).isEqualTo(EximeeIds.toUuid(instance.getId()));
        assertThat(runtimeService.getVariables(instance.getId())).isEmpty();
    }
}
