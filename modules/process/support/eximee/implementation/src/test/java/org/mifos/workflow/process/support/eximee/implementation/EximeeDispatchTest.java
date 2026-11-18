///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.support.eximee.implementation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mifos.workflow.process.core.MifosProcessConstants.MIFOS_WORKFLOW_PROCESS_ADAPTER_BEAN;
import static org.mifos.workflow.process.support.eximee.core.EximeeSupportConstants.EXIMEE_WORKFLOW_SUPPORT_PROPERTIES_ENABLED;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eximeebpms.bpm.engine.HistoryService;
import org.eximeebpms.bpm.engine.RepositoryService;
import org.eximeebpms.bpm.engine.RuntimeService;
import org.junit.jupiter.api.Test;
import org.mifos.workflow.process.support.eximee.implementation.EximeeDispatchTestApplication.GreetUsecase;
import org.mifos.workflow.process.support.eximee.implementation.EximeeDispatchTestApplication.ShoutFlow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tools.jackson.databind.json.JsonMapper;

/// Runs BPMN service tasks that call `EximeeAdapter` on a real Eximee engine backed by PostgreSQL, and checks
/// that the input variable reaches the dispatched usecase or flow and its output lands in the output variable.
@Slf4j
@SpringBootTest(
        classes = EximeeDispatchTestApplication.class,
        properties = EXIMEE_WORKFLOW_SUPPORT_PROPERTIES_ENABLED + "=true")
final class EximeeDispatchTest {
    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private JsonMapper jsonMapper;

    @Test
    void dispatchesServiceTaskToUsecaseByCanonicalName() {
        var key = deploy(GreetUsecase.Request.class.getCanonicalName());

        var instance = runtimeService.startProcessInstanceByKey(key, Map.of("input", "{\"name\":\"Ada\"}"));

        var output = (String) historicVariables(instance.getId()).get("output");
        assertThat(jsonMapper.readValue(output, GreetUsecase.Response.class))
                .isEqualTo(new GreetUsecase.Response("Hello Ada"));

        log.warn("Result: {}", output);
    }

    @Test
    void dispatchesServiceTaskToFlowByBinaryName() {
        var key = deploy(ShoutFlow.Ctx.class.getName());

        var instance = runtimeService.startProcessInstanceByKey(key, Map.of("input", "{\"text\":\"hi\"}"));

        var output = (String) historicVariables(instance.getId()).get("output");
        assertThat(jsonMapper.readValue(output, ShoutFlow.Ctx.class)).isEqualTo(new ShoutFlow.Ctx("hi", "HI"));

        log.warn("Result: {}", output);
    }

    @Test
    void keepsOutputAcrossWaitState() {
        var key = unique("dispatch");

        repositoryService
                .createDeployment()
                .addString(key + ".bpmn", serviceTaskThenWait(key, GreetUsecase.Request.class.getCanonicalName()))
                .deploy();

        var instance = runtimeService.startProcessInstanceByKey(key, Map.of("input", "{\"name\":\"Grace\"}"));
        assertThat(instance.isEnded()).isFalse();

        var output = (String) runtimeService.getVariable(instance.getId(), "output");
        assertThat(jsonMapper.readValue(output, GreetUsecase.Response.class))
                .isEqualTo(new GreetUsecase.Response("Hello Grace"));

        log.warn("Result: {}", output);
    }

    @Test
    void failsAndRollsBackWhenInputIsMissing() {
        var type = GreetUsecase.Request.class.getCanonicalName();
        var key = deploy(type);

        assertThatThrownBy(() -> runtimeService.startProcessInstanceByKey(key))
                .hasRootCauseInstanceOf(IllegalArgumentException.class)
                .rootCause()
                .hasMessageContaining("'input'")
                .hasMessageContaining(type);
        assertThat(historyService
                        .createHistoricProcessInstanceQuery()
                        .processDefinitionKey(key)
                        .count())
                .isZero();
    }

    @Test
    void failsAndRollsBackForUnknownType() {
        var key = deploy("com.acme.Unknown");
        Map<String, Object> variables = Map.of("input", "{}");

        assertThatThrownBy(() -> runtimeService.startProcessInstanceByKey(key, variables))
                .hasRootCauseInstanceOf(IllegalArgumentException.class)
                .rootCause()
                .hasMessageContaining("com.acme.Unknown");
        assertThat(historyService
                        .createHistoricProcessInstanceQuery()
                        .processDefinitionKey(key)
                        .count())
                .isZero();
    }

    private String deploy(String type) {
        var key = unique("dispatch");

        repositoryService
                .createDeployment()
                .addString(key + ".bpmn", serviceTask(key, type))
                .deploy();

        return key;
    }

    private Map<String, Object> historicVariables(String processId) {
        var variables = new HashMap<String, Object>();

        historyService
                .createHistoricVariableInstanceQuery()
                .processInstanceId(processId)
                .list()
                .forEach(v -> variables.put(v.getName(), v.getValue()));

        return variables;
    }

    private static String unique(String prefix) {
        return prefix + "-" + UUID.randomUUID();
    }

    /// start -> service task dispatching `type` -> end.
    private static String serviceTask(String key, String type) {
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
                         xmlns:camunda="http://camunda.org/schema/1.0/bpmn"
                         targetNamespace="http://mifos.org/workflow/test">
              <process id="%s" isExecutable="true" camunda:historyTimeToLive="P1D">
                <startEvent id="start"/>
                <sequenceFlow id="toDispatch" sourceRef="start" targetRef="dispatch"/>
                <serviceTask id="dispatch" camunda:expression="${%s.execute(execution, '%s')}"/>
                <sequenceFlow id="toEnd" sourceRef="dispatch" targetRef="end"/>
                <endEvent id="end"/>
              </process>
            </definitions>
            """.formatted(key, MIFOS_WORKFLOW_PROCESS_ADAPTER_BEAN, type);
    }

    /// start -> service task dispatching `type` -> user task (wait state) -> end.
    private static String serviceTaskThenWait(String key, String type) {
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
                         xmlns:camunda="http://camunda.org/schema/1.0/bpmn"
                         targetNamespace="http://mifos.org/workflow/test">
              <process id="%s" isExecutable="true" camunda:historyTimeToLive="P1D">
                <startEvent id="start"/>
                <sequenceFlow id="toDispatch" sourceRef="start" targetRef="dispatch"/>
                <serviceTask id="dispatch" camunda:expression="${%s.execute(execution, '%s')}"/>
                <sequenceFlow id="toWait" sourceRef="dispatch" targetRef="wait"/>
                <userTask id="wait"/>
                <sequenceFlow id="toEnd" sourceRef="wait" targetRef="end"/>
                <endEvent id="end"/>
              </process>
            </definitions>
            """.formatted(key, MIFOS_WORKFLOW_PROCESS_ADAPTER_BEAN, type);
    }
}
