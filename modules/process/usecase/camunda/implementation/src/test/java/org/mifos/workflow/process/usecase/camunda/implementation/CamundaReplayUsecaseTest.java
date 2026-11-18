///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.usecase.camunda.implementation;

import static org.assertj.core.api.Assertions.assertThat;

import io.camunda.process.test.api.CamundaAssert;
import io.camunda.process.test.api.assertions.ProcessInstanceSelectors;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mifos.workflow.process.core.usecase.MifosProcessReplayUsecase;
import org.springframework.beans.factory.annotation.Autowired;

final class CamundaReplayUsecaseTest extends CamundaUsecaseTestSupport {
    @Autowired
    private MifosProcessReplayUsecase usecase;

    @Test
    void replaysProcessWithMergedVariables() {
        var key = unique("replay");
        deploy(key, CamundaTestProcesses.straightThrough(key));
        var source = start(key, Map.of("a", 1, "b", 2));
        CamundaAssert.assertThat(source).isCompleted();
        awaitVariables(source.getProcessInstanceKey(), 2);

        var response = usecase.execute(MifosProcessReplayUsecase.Request.builder()
                .sourceProcessId(CamundaIds.toUuid(source.getProcessInstanceKey()))
                .variables(Map.of("b", 3))
                .build());

        CamundaAssert.assertThat(ProcessInstanceSelectors.byKey(CamundaIds.toKey(response.getId())))
                .isCompleted()
                .hasVariable("a", 1)
                .hasVariable("b", 3);
    }

    @Test
    void replaysProcessWithSourceVariablesOnly() {
        var key = unique("replay");
        deploy(key, CamundaTestProcesses.straightThrough(key));
        var source = start(key, Map.of("a", 1));
        CamundaAssert.assertThat(source).isCompleted();
        awaitVariables(source.getProcessInstanceKey(), 1);

        var response = usecase.execute(MifosProcessReplayUsecase.Request.builder()
                .sourceProcessId(CamundaIds.toUuid(source.getProcessInstanceKey()))
                .build());

        assertThat(response.getId()).isNotNull().isNotEqualTo(CamundaIds.toUuid(source.getProcessInstanceKey()));
        CamundaAssert.assertThat(ProcessInstanceSelectors.byKey(CamundaIds.toKey(response.getId())))
                .isCompleted()
                .hasVariable("a", 1);
    }
}
