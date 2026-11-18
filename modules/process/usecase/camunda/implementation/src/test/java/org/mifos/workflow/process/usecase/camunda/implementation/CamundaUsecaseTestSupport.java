///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.usecase.camunda.implementation;

import static org.awaitility.Awaitility.await;
import static org.mifos.workflow.process.core.MifosProcessConstants.MIFOS_WORKFLOW_PROCESS_PROPERTIES_ENGINE;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.response.DeploymentEvent;
import io.camunda.client.api.response.ProcessInstanceEvent;
import io.camunda.process.test.api.CamundaSpringProcessTest;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/// Shared setup for the Camunda usecase tests: a Camunda 8 runtime started by Camunda Process Test through
/// Testcontainers. Tests isolate themselves by using unique process ids, assignees and signal names.
///
/// Commands are applied by the broker right away, but the search API (tasks, history, variables) catches up a moment
/// later, so tests that read through it wait with `eventually`.
@SpringBootTest(
        classes = CamundaTestApplication.class,
        properties = MIFOS_WORKFLOW_PROCESS_PROPERTIES_ENGINE + "=CAMUNDA")
@CamundaSpringProcessTest
abstract class CamundaUsecaseTestSupport {
    protected static final Duration TIMEOUT = Duration.ofSeconds(30);

    @Autowired
    protected CamundaClient client;

    protected static String unique(String prefix) {
        return prefix + "-" + UUID.randomUUID();
    }

    protected DeploymentEvent deploy(String key, String bpmn) {
        return client.newDeployResourceCommand()
                .addResourceStringUtf8(bpmn, key + ".bpmn")
                .send()
                .join();
    }

    protected ProcessInstanceEvent start(String key) {
        return start(key, Map.of());
    }

    protected ProcessInstanceEvent start(String key, Map<String, Object> variables) {
        return client.newCreateInstanceCommand()
                .bpmnProcessId(key)
                .latestVersion()
                .variables(variables)
                .send()
                .join();
    }

    /// Waits until the process instance's user task is visible to the search API and returns its key.
    protected long userTaskKey(long processInstanceKey) {
        return await().atMost(TIMEOUT)
                .until(
                        () -> client.newUserTaskSearchRequest()
                                .filter(filter -> filter.processInstanceKey(processInstanceKey))
                                .send()
                                .join()
                                .items(),
                        tasks -> tasks.size() == 1)
                .getFirst()
                .getUserTaskKey();
    }

    /// Waits until the process instance's process-level variables are visible to the search API.
    protected void awaitVariables(long processInstanceKey, int count) {
        await().atMost(TIMEOUT)
                .until(
                        () -> client.newVariableSearchRequest()
                                .filter(filter -> filter.processInstanceKey(processInstanceKey)
                                        .scopeKey(processInstanceKey))
                                .send()
                                .join()
                                .items()
                                .size(),
                        size -> size == count);
    }

    protected static void eventually(Runnable assertion) {
        await().atMost(TIMEOUT).untilAsserted(assertion::run);
    }
}
