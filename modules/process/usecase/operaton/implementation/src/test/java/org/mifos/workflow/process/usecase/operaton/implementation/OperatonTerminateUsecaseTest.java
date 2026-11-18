///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.usecase.operaton.implementation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.mifos.workflow.process.core.usecase.MifosProcessTerminateUsecase;
import org.springframework.beans.factory.annotation.Autowired;

class OperatonTerminateUsecaseTest extends OperatonUsecaseTestSupport {
    @Autowired
    private MifosProcessTerminateUsecase usecase;

    @Test
    void terminatesRunningProcess() {
        var key = unique("terminate");
        deploy(key, OperatonTestProcesses.userTask(key, unique("user")));
        var instance = start(key);

        var response = usecase.execute(MifosProcessTerminateUsecase.Request.builder()
                .processId(instance.getId())
                .reason("no longer needed")
                .build());

        assertThat(response.getId()).isEqualTo(OperatonIds.toUuid(instance.getId()));
        assertThat(runtimeService
                        .createProcessInstanceQuery()
                        .processInstanceId(instance.getId())
                        .count())
                .isZero();
        var historic = historyService
                .createHistoricProcessInstanceQuery()
                .processInstanceId(instance.getId())
                .singleResult();
        assertThat(historic.getEndTime()).isNotNull();
        assertThat(historic.getDeleteReason()).isEqualTo("no longer needed");
    }
}
