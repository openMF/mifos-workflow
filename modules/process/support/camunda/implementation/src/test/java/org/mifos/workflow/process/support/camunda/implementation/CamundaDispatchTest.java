///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.support.camunda.implementation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mifos.workflow.process.core.MifosProcessConstants.MIFOS_WORKFLOW_PROCESS_ADAPTER_BEAN;
import static org.mifos.workflow.process.support.camunda.core.CamundaSupportConstants.CAMUNDA_WORKFLOW_SUPPORT_PROPERTIES_ENABLED;
import static org.mifos.workflow.process.support.camunda.core.CamundaSupportConstants.CAMUNDA_WORKFLOW_SUPPORT_TYPE_HEADER;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.response.ProcessInstanceEvent;
import io.camunda.process.test.api.CamundaAssert;
import io.camunda.process.test.api.CamundaSpringProcessTest;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mifos.workflow.process.support.camunda.implementation.CamundaDispatchTestApplication.GreetUsecase;
import org.mifos.workflow.process.support.camunda.implementation.CamundaDispatchTestApplication.ShoutFlow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tools.jackson.databind.json.JsonMapper;

/// Runs BPMN service tasks of the `mifosAdapter` job type on a real Camunda 8 runtime (started by Camunda Process
/// Test through Testcontainers), and checks that the input variable reaches the dispatched usecase or flow, its
/// output completes the job, and unusable jobs raise an incident.
@SpringBootTest(
        classes = CamundaDispatchTestApplication.class,
        properties = CAMUNDA_WORKFLOW_SUPPORT_PROPERTIES_ENABLED + "=true")
@CamundaSpringProcessTest
final class CamundaDispatchTest {
    @Autowired
    private CamundaClient client;

    @Autowired
    private JsonMapper jsonMapper;

    @Test
    void dispatchesServiceTaskToUsecaseByCanonicalName() {
        var key = deploy(serviceTask(GreetUsecase.Request.class.getCanonicalName()));

        var output = runToCompletion(key, Map.of("input", "{\"name\":\"Ada\"}"));

        assertThat(jsonMapper.readValue(output, GreetUsecase.Response.class))
                .isEqualTo(new GreetUsecase.Response("Hello Ada"));
    }

    @Test
    void dispatchesServiceTaskToFlowByBinaryName() {
        var key = deploy(serviceTask(ShoutFlow.Ctx.class.getName()));

        var output = runToCompletion(key, Map.of("input", "{\"text\":\"hi\"}"));

        assertThat(jsonMapper.readValue(output, ShoutFlow.Ctx.class)).isEqualTo(new ShoutFlow.Ctx("hi", "HI"));
    }

    @Test
    void acceptsJsonObjectVariableAsInput() {
        var key = deploy(serviceTask(GreetUsecase.Request.class.getCanonicalName()));

        var output = runToCompletion(key, Map.of("input", Map.of("name", "Linus")));

        assertThat(jsonMapper.readValue(output, GreetUsecase.Response.class))
                .isEqualTo(new GreetUsecase.Response("Hello Linus"));
    }

    @Test
    void keepsOutputAcrossWaitState() {
        var key = deploy(serviceTaskThenWait(GreetUsecase.Request.class.getCanonicalName()));

        var instance = start(key, Map.of("input", "{\"name\":\"Grace\"}"));

        // single-field response, so the JSON text is deterministic
        CamundaAssert.assertThat(instance)
                .isActive()
                .hasCompletedElements("dispatch")
                .hasVariable("output", "{\"greeting\":\"Hello Grace\"}");
    }

    @Test
    void raisesIncidentWhenInputIsMissing() {
        var key = deploy(serviceTask(GreetUsecase.Request.class.getCanonicalName()));

        var instance = start(key, Map.of());

        CamundaAssert.assertThat(instance).isActive().hasActiveIncidents();
    }

    @Test
    void raisesIncidentForUnknownType() {
        var key = deploy(serviceTask("com.acme.Unknown"));

        var instance = start(key, Map.of("input", "{}"));

        CamundaAssert.assertThat(instance).isActive().hasActiveIncidents();
    }

    private String deploy(String bpmnTemplate) {
        var key = "dispatch-" + UUID.randomUUID();
        client.newDeployResourceCommand()
                .addResourceStringUtf8(bpmnTemplate.formatted(key), key + ".bpmn")
                .send()
                .join();
        return key;
    }

    private ProcessInstanceEvent start(String key, Map<String, Object> variables) {
        return client.newCreateInstanceCommand()
                .bpmnProcessId(key)
                .latestVersion()
                .variables(variables)
                .send()
                .join();
    }

    private String runToCompletion(String key, Map<String, Object> variables) {
        var result = client.newCreateInstanceCommand()
                .bpmnProcessId(key)
                .latestVersion()
                .variables(variables)
                .withResult()
                .send()
                .join();
        return (String) result.getVariablesAsMap().get("output");
    }

    /// start -> `mifosAdapter` service task dispatching `type` -> end; the process id is filled in by `deploy`.
    private static String serviceTask(String type) {
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
                         xmlns:zeebe="http://camunda.org/schema/zeebe/1.0"
                         targetNamespace="http://mifos.org/workflow/test">
              <process id="%%s" isExecutable="true">
                <startEvent id="start"/>
                <sequenceFlow id="toDispatch" sourceRef="start" targetRef="dispatch"/>
                %s
                <sequenceFlow id="toEnd" sourceRef="dispatch" targetRef="end"/>
                <endEvent id="end"/>
              </process>
            </definitions>
            """.formatted(dispatchTask(type));
    }

    /// start -> `mifosAdapter` service task dispatching `type` -> user task (wait state) -> end.
    private static String serviceTaskThenWait(String type) {
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
                         xmlns:zeebe="http://camunda.org/schema/zeebe/1.0"
                         targetNamespace="http://mifos.org/workflow/test">
              <process id="%%s" isExecutable="true">
                <startEvent id="start"/>
                <sequenceFlow id="toDispatch" sourceRef="start" targetRef="dispatch"/>
                %s
                <sequenceFlow id="toWait" sourceRef="dispatch" targetRef="wait"/>
                <userTask id="wait"/>
                <sequenceFlow id="toEnd" sourceRef="wait" targetRef="end"/>
                <endEvent id="end"/>
              </process>
            </definitions>
            """.formatted(dispatchTask(type));
    }

    private static String dispatchTask(String type) {
        return """
            <serviceTask id="dispatch">
                  <extensionElements>
                    <zeebe:taskDefinition type="%s"/>
                    <zeebe:taskHeaders>
                      <zeebe:header key="%s" value="%s"/>
                    </zeebe:taskHeaders>
                  </extensionElements>
                </serviceTask>\
            """.formatted(MIFOS_WORKFLOW_PROCESS_ADAPTER_BEAN, CAMUNDA_WORKFLOW_SUPPORT_TYPE_HEADER, type);
    }
}
