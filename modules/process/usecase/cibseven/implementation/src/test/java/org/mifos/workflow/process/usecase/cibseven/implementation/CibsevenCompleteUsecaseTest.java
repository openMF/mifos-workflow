///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.usecase.cibseven.implementation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mifos.workflow.process.core.usecase.MifosProcessCompleteUsecase;
import org.springframework.beans.factory.annotation.Autowired;

class CibsevenCompleteUsecaseTest extends CibsevenUsecaseTestSupport {
    @Autowired
    private MifosProcessCompleteUsecase usecase;

    @Test
    void completesTaskAndFinishesProcess() {
        var key = unique("complete");
        deploy(key, CibsevenTestProcesses.userTask(key, unique("user")));
        var instance = start(key);
        var task = taskService
                .createTaskQuery()
                .processInstanceId(instance.getId())
                .singleResult();

        var response = usecase.execute(MifosProcessCompleteUsecase.Request.builder()
                .taskId(task.getId())
                .variables(Map.of("approved", true))
                .build());

        assertThat(response.getId()).isEqualTo(CibsevenIds.toUuid(task.getId()));
        assertThat(taskService.createTaskQuery().taskId(task.getId()).count()).isZero();
        var historic = historyService
                .createHistoricProcessInstanceQuery()
                .processInstanceId(instance.getId())
                .singleResult();
        assertThat(historic.getEndTime()).isNotNull();
        assertThat(historicVariables(instance.getId())).containsEntry("approved", true);
    }

    @Test
    void completesTaskWithoutVariables() {
        var key = unique("complete");
        deploy(key, CibsevenTestProcesses.userTask(key, unique("user")));
        var instance = start(key);
        var task = taskService
                .createTaskQuery()
                .processInstanceId(instance.getId())
                .singleResult();

        usecase.execute(MifosProcessCompleteUsecase.Request.builder()
                .taskId(task.getId())
                .build());

        assertThat(taskService.createTaskQuery().taskId(task.getId()).count()).isZero();
    }
}
