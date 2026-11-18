///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.usecase.eximee.implementation;

import static org.mifos.workflow.process.core.MifosProcessConstants.MIFOS_WORKFLOW_PROCESS_PROPERTIES_ENGINE;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.eximeebpms.bpm.engine.HistoryService;
import org.eximeebpms.bpm.engine.RepositoryService;
import org.eximeebpms.bpm.engine.RuntimeService;
import org.eximeebpms.bpm.engine.TaskService;
import org.eximeebpms.bpm.engine.repository.Deployment;
import org.eximeebpms.bpm.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/// Shared setup for the Eximee usecase tests. All test classes share one cached Spring context, so one engine and
/// one PostgreSQL container; tests isolate themselves by using unique process keys, assignees and signal names.
@SpringBootTest(
        classes = EximeeTestApplication.class,
        properties = MIFOS_WORKFLOW_PROCESS_PROPERTIES_ENGINE + "=EXIMEE")
abstract class EximeeUsecaseTestSupport {
    @Autowired
    protected RepositoryService repositoryService;

    @Autowired
    protected RuntimeService runtimeService;

    @Autowired
    protected TaskService taskService;

    @Autowired
    protected HistoryService historyService;

    protected static String unique(String prefix) {
        return prefix + "-" + UUID.randomUUID();
    }

    protected Deployment deploy(String key, String bpmn) {
        return repositoryService
                .createDeployment()
                .addString(key + ".bpmn", bpmn)
                .name(key)
                .deploy();
    }

    protected ProcessInstance start(String key) {
        return runtimeService.startProcessInstanceByKey(key);
    }

    protected ProcessInstance start(String key, Map<String, Object> variables) {
        return runtimeService.startProcessInstanceByKey(key, variables);
    }

    protected Map<String, Object> historicVariables(String processId) {
        var variables = new HashMap<String, Object>();
        historyService
                .createHistoricVariableInstanceQuery()
                .processInstanceId(processId)
                .list()
                .forEach(v -> variables.put(v.getName(), v.getValue()));
        return variables;
    }
}
